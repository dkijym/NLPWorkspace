#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <ctype.h>
#include <getopt.h>
#include <values.h>
#include <limits.h>

#include <errno.h>
#include <unistd.h>
#include <sys/stat.h> //mkdir

#include "libstrfunc.h" // for base64_encoding

#include "define.h"
#include "libpst.h"
#include "common.h"
#include "timeconv.h"

#define OUTPUT_TEMPLATE "%s"
#define OUTPUT_KMAIL_DIR_TEMPLATE ".%s.directory"
#define KMAIL_INDEX ".%s.index"

#define VERSION "0.3.4"
// max size of the c_time char*. It will store the date of the email
#define C_TIME_SIZE 500
#define PERM_DIRS 0777

// macro used for creating directories
#ifndef WIN32
#define D_MKDIR(x) mkdir(x, PERM_DIRS)
#else
#define D_MKDIR(x) mkdir(x)
#endif

void write_email_body(FILE *f, char *body);
char * removeCR (char *c);
int usage();
int version();
char *mk_kmail_dir(char*);
int close_kmail_dir();
char *mk_recurse_dir(char*);
int close_recurse_dir();
char * my_stristr(char *haystack, char *needle);

char *prog_name;
char *output_dir = ".";

#define MODE_NORMAL 0
#define MODE_KMAIL 1
#define MODE_RECURSE 2

int main(int argc, char** argv) {
  pst_item *item = NULL;
  pst_file pstfile;
  pst_desc_ll *d_ptr;
  char * fname = NULL;
  time_t em_time;
  char * c_time;
  int c,x;
  int mode = MODE_NORMAL;
  int overwrite = 0;

  //  int encrypt = 0;
  //  FILE *fp;
  char *enc; // base64 encoded attachment
  char *boundary = NULL, *b1, *b2; // the boundary marker between multipart sections
  char *temp = NULL; //temporary char pointer

  struct file_ll {
    char *name;
    FILE * output;
    int email_count;
    int type;
    struct file_ll *next;
  } *f, *head;

  prog_name = argv[0];

  while ((c = getopt(argc, argv, "hko:rVw"))!= -1) {
    switch (c) {
    case 'h':
      usage();
      exit(0);
      break;
    case 'V':
      version();
      exit(0);
      break;
    case 'k':
      mode = MODE_KMAIL;
      break;
    case 'o':
      output_dir = optarg;
      break;
    case 'r':
      mode = MODE_RECURSE;
      break;
    case 'w':
      overwrite = 1;
      break;
    default:
      usage();
      exit(1);
      break;
    }
  }

  if (argc > optind) {
    fname = argv[optind];
  } else {
    usage();
    exit(2);
  }

#ifndef DEBUG_ALL
  printf("Opening PST file and indexes...\n");
#endif  

  DEBUG_MAIN(("main: Opening PST file\n"));
  RET_DERROR(pst_open(&pstfile, fname, "r"), 1, ("No File\n"));
  DEBUG_MAIN(("main: Loading Indexes\n"));
  RET_DERROR(pst_load_index(&pstfile), 2, ("Index Error\n"));

  if (chdir(output_dir)) {
    x = errno;
    pst_close(&pstfile);
    DIE(("main: Cannot change to output dir %s: %s\n", output_dir, strerror(x)));
  }

#ifndef DEBUG_ALL
  printf("About to start processing first record...\n");
#endif

  d_ptr = pstfile.d_head; // first record is main record
  if ((item = _pst_parse_item(&pstfile, d_ptr)) == NULL || item->message_store == NULL) {
    DEBUG_MAIN(("main: Could not get root record\n"));
    return -1;
  }
  
  DEBUG_MAIN(("main: Root Folder Name: %s\n", item->file_as));
  DEBUG_MAIN(("main: Desc ID of TOPF: %#x\n", item->message_store->top_of_personal_folder->id));
  
  f = (struct file_ll*) malloc(sizeof(struct file_ll));
  f->email_count = 0;
  f->next = NULL;
  head = f;
  if (mode == MODE_KMAIL)
    f->name = mk_kmail_dir(item->file_as);
  else if (mode == MODE_RECURSE)
    f->name = mk_recurse_dir(item->file_as);
  else {
    f->name = (char*) malloc(strlen(item->file_as)+strlen(OUTPUT_TEMPLATE)+1);
    sprintf(f->name, OUTPUT_TEMPLATE, item->file_as);
  }

  if (overwrite != 1) { // if overwrite is set to 1 we keep the existing name and don't modify anything
    temp = (char*) malloc (strlen(f->name)+10); //enough room for 10 digits
    sprintf(temp, "%s", f->name);
    x = 0;
    while ((f->output = fopen(temp, "r")) != NULL) {
      DEBUG_MAIN(("main: need to increase filename cause one already exists with that name\n"));
      DEBUG_MAIN(("main: - increasing it to %s%d\n", f->name, x));
      x++;
      sprintf(temp, "%s%08d", f->name, x);
      DEBUG_MAIN(("main: - trying \"%s\"\n", temp));
      if (x == 99999999) {
	DIE(("main: Why can I not create a folder %s? I have tried %i extensions...\n", f->name, x));
      }
      fclose(f->output);
    }
    if (x > 0) { //then the f->name should change
      free (f->name);
      f->name = temp;
    } else {
      free (temp);
    }
  }

  if ((f->output = fopen(f->name, "w")) == NULL) {
    DIE(("main: Could not open file \"%s\" for write\n", f->name));
  }
  f->type = item->type;

  d_ptr = _pst_getDptr(&pstfile, item->message_store->top_of_personal_folder->id);
  if (item){
    _pst_freeItem(item);
    item = NULL;
  }

  /*  if ((item = _pst_parse_item(&pstfile, d_ptr)) == NULL || item->folder == NULL) {
    DEBUG_MAIN(("main: Could not get \"Top Of Personal Folder\" record\n"));
    return -2;
    }*/
  d_ptr = d_ptr->child; // do the children of TOPF

#ifndef DEBUG_ALL
  printf("Processing items...\n");
#endif

  DEBUG_MAIN(("main: About to do email stuff\n"));
  while (d_ptr != NULL) {
    DEBUG_MAIN(("main: New item record\n"));
    if (d_ptr->desc == NULL) {
      WARN(("main: ERROR ?? item's desc record is NULL\n"));
      goto check_parent;
    }
    DEBUG_MAIN(("main: Desc Email ID %#x [d_ptr->id = %#x]\n", d_ptr->desc->id, d_ptr->id));

    item = _pst_parse_item(&pstfile, d_ptr);
    DEBUG_MAIN(("main: About to process item\n"));
    if (item != NULL && item->email != NULL && item->email->subject != NULL &&
	item->email->subject->subj != NULL) {
      //      DEBUG_EMAIL(("item->email->subject = %p\n", item->email->subject));
      //      DEBUG_EMAIL(("item->email->subject->subj = %p\n", item->email->subject->subj));
    }
    if (item != NULL) {
      if (item->message_store != NULL) { 
	// there should only be one message_store, and we have already done it
	DIE(("main: A second message_store has been found. Sorry, this must be an error.\n"));
      }
      if (item->folder != NULL) { //if this is a folder, we want to recurse into it
	if (d_ptr->child != NULL) {
#ifndef DEBUG_ALL
	  printf("Processing Folder \"%s\"\n", item->file_as);
#endif
	  DEBUG_MAIN(("main: I think I may try to go into folder \"%s\"\n", item->file_as));
	  f = (struct file_ll*) malloc(sizeof(struct file_ll));
	  f->next = head;
	  f->email_count = 0;
	  f->type = item->type; 
	  head = f;

	  temp = item->file_as;
	  while ((temp = strpbrk(temp, "/\\")) != NULL) {
	    // while there are characters in the second string that we don't want
	    *temp = '_'; //replace them with an underscore
	  }

	  if (mode == MODE_KMAIL)
	    f->name = mk_kmail_dir(item->file_as); //create directory and form filename
	  else if (mode == MODE_RECURSE) 
	    f->name = mk_recurse_dir(item->file_as);
	  else {
	    f->name = (char*) malloc(strlen(item->file_as)+strlen(OUTPUT_TEMPLATE+1));
	    sprintf(f->name, OUTPUT_TEMPLATE, item->file_as);
	  }

	  if (overwrite != 1) {
	    temp = (char*) malloc (strlen(f->name)+10); //enough room for 10 digits
	    sprintf(temp, "%s", f->name);
	    x = 0;
	    while ((f->output = fopen(temp, "r")) != NULL) {
	      DEBUG_MAIN(("main: need to increase filename cause one already exists with that name\n"));
	      DEBUG_MAIN(("main: - increasing it to %s%d\n", f->name, x));
	      x++;
	      sprintf(temp, "%s%08d", f->name, x);
	      DEBUG_MAIN(("main: - trying \"%s\"\n", f->name));
	      if (x == 99999999) {
		DIE(("main: Why can I not create a folder %s? I have tried %i extensions...\n", f->name, x));
	      }
	      fclose(f->output);
	    }
	    if (x > 0) { //then the f->name should change
	      free (f->name);
	      f->name = temp;
	    } else {
	      free(temp);
	    }
	  }

	  DEBUG_MAIN(("main: f->name = %s\nitem->folder_name = %s\n", f->name, item->file_as));
	  if ((f->output = fopen(f->name, "w")) == NULL) {
	    DIE(("main: Could not open file \"%s\" for write\n", f->name));
	  }	
	  d_ptr = d_ptr->child;
	} else {
	  DEBUG_MAIN(("main: Folder has NO children\n"));
	  goto check_parent;
	}
	_pst_freeItem(item);
	item = NULL; // just for the odd situations!
	continue;
      } else if (item->contact != NULL) {
	// deal with a contact
	// write them to the file, one per line in this format
	// Desc Name <email@address>\n
	DEBUG_MAIN(("main: Processing Contact\n"));
	if (f->type != PST_TYPE_CONTACT) {
	  DEBUG_MAIN(("main: I have a contact, but the folder isn't a contacts folder. "
		     "Will process anyway\n"));
	}
	if (item->type != PST_TYPE_CONTACT) {
	  DEBUG_MAIN(("main: I have an item that has contact info, but doesn't say that"
		     " it is a contact. Type is \"%s\"\n", item->ascii_type));
	  DEBUG_MAIN(("main: Processing anyway\n"));
	}
	if (item->email == NULL || item->email->subject == NULL || 
	    item->contact == NULL) { // this is an incorrect situation. Inform user
	  DEBUG_MAIN(("main: ERROR. This contact has not been fully parsed. one of the pre-requisties is NULL\n"));
	} else {
	  fprintf(f->output, "%s <%s>\n", item->email->subject->subj, item->contact->address1);
        }
      } else if (item->email != NULL && item->type == PST_TYPE_NOTE) {

	DEBUG_MAIN(("main: seen an email\n"));
	if (item != NULL && item->email != NULL && item->email->subject != NULL &&
	    item->email->subject->subj != NULL) {
	  // just debug print the subject
	  DEBUG_EMAIL(("item->email->subject->subj = %p\n", item->email->subject->subj));
	}

	f->email_count++;
	
	// convert the sent date if it exists, or set it to a fixed date
	if (item->email->sent_date != NULL) {
	  em_time = FileTimeToUnixTime(item->email->sent_date, 0);
	  c_time = ctime(&em_time);
	  if (c_time != NULL)
	    c_time[strlen(c_time)-1] = '\0'; //remove end \n
	  else
	    c_time = "Fri Dec 28 12:06:21 2001";
	} else 
	  c_time= "Fri Dec 28 12:06:21 2001";

	// if the boundary is still set from the previous run, then free it
	if (boundary != NULL) {
	  free (boundary);
	  boundary = NULL;
	}

	if (item->attach ||(item->email->body && item->email->htmlbody)) {
	  // we will need a boundary in this situation
	  if (item->email->header != NULL ) {
	    // see if there is a boundary variable there
	    // this search MUST be made case insensitive (DONE).
	    // Also, some check to find out if we
	    // are looking at the boundary associated with content-type, and that the content
	    // type really is "multipart"
	    if ((b2 = my_stristr(item->email->header, "boundary=")) != NULL) {
	      b2 += strlen("boundary="); // move boundary to first char of marker
	      
	      if (*b2 == '"') { 
		b2++;
		b1 = strchr(b2, '"'); // find terminating quote
	      } else {
		b1 = b2;
		while (isgraph(*b1)) // find first char that isn't part of boundary
		b1++;
	      }
	      
	      boundary = malloc ((b1-b2)+1); //malloc that length
	      memset (boundary, 0, (b1-b2)+1);  // blank it
	      strncpy(boundary, b2, b1-b2); // copy boundary to another variable
	      b1 = b2 = boundary;
	      while (*b2 != '\0') { // remove any CRs and Tabs
		if (*b2 != '\n' && *b2 != '\r' && *b2 != '\t') {
		  *b1 = *b2;
		  b1++;
		}
		b2++;
	    }
	      *b1 = '\0';

	      DEBUG_MAIN(("main: Found boundary of - %s\n", boundary));
	    } else {
	      DEBUG_MAIN(("main: boundary not found in header\n"));
	    }
	  } else {
	    DEBUG_EMAIL(("main: must create own boundary. oh dear.\n"));
	    boundary = malloc(50 * sizeof(char)); // allow 50 chars for boundary
	    boundary[0] = '\0';
	    sprintf(boundary, "--boundary-LibPST-iamunique-%i_-_-", (int)time(NULL));
	    DEBUG_EMAIL(("main: created boundary is %s\n", boundary));
	  }
	}

	DEBUG_MAIN(("main: About to print Header\n"));

	if (item != NULL && item->email != NULL && item->email->subject != NULL &&
	    item->email->subject->subj != NULL) {
	  DEBUG_EMAIL(("item->email->subject->subj = %p\n", item->email->subject->subj));
	}
	if (item->email->header != NULL) {
	  // some of the headers we get from the file are not properly defined.
	  // they can contain some email stuff too. We will cut off the header
	  // when we see a \n\n or \r\n\r\n

	  removeCR(item->email->header);

	  temp = strstr(item->email->header, "\n\n");

	  if (temp != NULL) {
	    DEBUG_MAIN(("main: Found body text in header\n"));
	    *temp = '\0';
	  }
	  
	  //	  DEBUG_MAIN(("main: Header = \"%s\"\n", item->email->header));
	  
	  fprintf(f->output, "From \"%s\" %s\n%s\n",
		  item->email->outlook_sender_name, c_time, /*item->sender_address,*/ item->email->header);
	} else {
	  //make up our own header!
	  fprintf(f->output, "From \"%s\" %s\n", item->email->outlook_sender_name, c_time);
	  fprintf(f->output, "From: \"%s\" <%s>\n", item->email->outlook_sender_name, item->email->outlook_sender);
	  if (item->email->subject != NULL) {
	    fprintf(f->output, "Subject: %s\n", item->email->subject->subj);
	  } else {
	    fprintf(f->output, "Subject: \n");
	  }
	  fprintf(f->output, "To: %s\n", item->email->sentto_address);
	  if (item->email->sent_date != NULL) {
	    c_time = (char*) malloc(C_TIME_SIZE);
	    strftime(c_time, C_TIME_SIZE, "%a, %d %b %Y %H:%M:%S %z", gmtime(&em_time));
	    fprintf(f->output, "Date: %s\n", c_time);
	    free(c_time);
	  }
	  if (item->attach != NULL) {
	    // write the boundary stuff if we have attachments
	    fprintf(f->output, "Content-type: multipart/mixed;\n\tboundary=\"%s\"\n",
		    boundary);
	  } else if (item->email->htmlbody && item->email->body) {
	    // else if we have an html and text body then tell it so
	    fprintf(f->output, "Content-type: multipart/alternate;\n\tboundary=\"%s\"\n",
		    boundary);
	  } else if (item->email->htmlbody) {
	    fprintf(f->output, "Content-type: text/html\n");
	  }
	  fprintf(f->output, "\n");
	}
	

	DEBUG_MAIN(("main: About to print Body\n"));

	if (item->email->body != NULL) {
	  if (boundary) {
	    fprintf(f->output, "\n--%s\n", boundary);
	    fprintf(f->output, "Content-type: text/plain\n\n");
	  }
	  removeCR(item->email->body);
	  write_email_body(f->output, item->email->body);
	}
	
	if (item->email->htmlbody != NULL) {
	  if (boundary) {
	    fprintf(f->output, "\n--%s\n", boundary);
	    fprintf(f->output, "Content-type: text/html\n\n");
	  }
	  removeCR(item->email->htmlbody);
	  write_email_body(f->output, item->email->htmlbody);
	} 

	// attachments
	item->current_attach = item->attach;
	while (item->current_attach != NULL) {
	  DEBUG_MAIN(("main: Attempting Attachment encoding\n"));
	  if (item->current_attach->data == NULL) {
	    DEBUG_MAIN(("main: Data of attachment is NULL!. Size is supposed to be %i\n", item->current_attach->size));
	  }
	  DEBUG_MAIN(("Attachment Size is %i\n", item->current_attach->size));
	  if ((enc = base64_encode (item->current_attach->data, item->current_attach->size)) == NULL) {
	    DEBUG_MAIN(("main: ERROR base64_encode returned NULL. Must have failed\n"));
	    item->current_attach = item->current_attach->next;
	    continue;
	  }
	  if (boundary) {
	    fprintf(f->output, "\n--%s\n", boundary);
	    fprintf(f->output, "Content-type: %s\n", item->current_attach->mimetype);
	    fprintf(f->output, "Content-transfer-encoding: base64\n");
	    if (item->current_attach->filename2 == NULL) {
	      fprintf(f->output, "Content-Disposition: inline\n\n");
	    } else {
	      fprintf(f->output, "Content-Disposition: attachment; filename=\"%s\"\n\n",
		      item->current_attach->filename2);
	    }
	  }
	  DEBUG_MAIN(("Attachment Size after encoding is %i\n", strlen(enc)));
	  fwrite(enc, 1, strlen(enc), f->output);
	  fprintf(f->output, "\n\n");
	  item->current_attach = item->current_attach->next;
	}

	DEBUG_MAIN(("main: Writing buffer between emails\n"));
	if (boundary)
	  fprintf(f->output, "\n--%s--\n", boundary);
	fprintf(f->output, "\n\n");
      } else {
	DEBUG_MAIN(("main: Unknown item type. %i. Ascii1=\"%s\"\n", 
		   item->type, item->ascii_type));
      }
    } else {
      DEBUG_MAIN(("main: A NULL item was seen\n"));
    }
    DEBUG_MAIN(("main: Going to next d_ptr\n"));
    if (boundary) {
      free(boundary);
      boundary = NULL;
    }
  check_parent:
    //    _pst_freeItem(item);
    while (d_ptr->next == NULL && d_ptr->parent != NULL) {
      DEBUG_MAIN(("main: Going to Parent\n"));
      head = f->next;
      fclose(f->output);
      DEBUG_MAIN(("main: Email Count for folder %s is %i\n", f->name, f->email_count));
      if (mode == MODE_KMAIL)
	close_kmail_dir();
      else if (mode == MODE_RECURSE)
	close_recurse_dir();
      free(f->name);
      free(f);
      f = head;
      if (head == NULL) { //we can't go higher. Must be at start?
	DEBUG_MAIN(("main: We are now trying to go above the highest level. I think we should stop\n"));
	break; //from main while loop
      }
      d_ptr = d_ptr->parent;
    }

    if (item != NULL) {
      DEBUG_MAIN(("main: Freeing memory used by item\n"));
      _pst_freeItem(item);
      item = NULL;
    }

    d_ptr = d_ptr->next;
    if (d_ptr == NULL) {
      DEBUG_MAIN(("main: d_ptr is now NULL\n"));
    }
  }	
#ifndef DEBUG_ALL
  printf("Finished.\n");
#endif

  DEBUG_MAIN(("main: Finished.\n"));
  pst_close(&pstfile);
  //  fclose(pstfile.fp);
  while (f != NULL) {
    fclose(f->output);
    free(f->name);
    if (mode == MODE_KMAIL)
      close_kmail_dir();
    else if (mode == MODE_RECURSE)
      close_recurse_dir();
    head = f->next;
    free (f);
    f = head;
  }


  return 0;
}

void write_email_body(FILE *f, char *body) {
  char *n = body;
  //  DEBUG_MAIN(("write_email_body(): \"%s\"\n", body));

  while (n != NULL) {
    if (strncmp(body, "From ", 5) == 0)
      fprintf(f, ">");
    if ((n = strchr(body, '\n'))) {
      n++;
      fwrite(body, n-body, 1, f); //write just a line
      
      body = n;
    }
  }
  fwrite(body, strlen(body), 1, f);
}

char * removeCR (char *c) {
  // converts /r/n to /n
  char *a, *b;
  a = b = c;
  while (*a != '\0') {
    *b = *a;
    if (*a != '\r')
      b++;
    a++;
  }
  *b = '\0';
  return c;
}
    
int usage() {
  version();
  printf("Usage: %s [OPTIONS] {PST FILENAME}\n", prog_name);
  printf("OPTIONS:\n");
  printf("\t-h\t- Help. This screen\n");
  printf("\t-k\t- KMail. Output in kmail format\n");
  printf("\t-o\t- Output Dir. Directory to write files to. CWD is changed *after* opening pst file\n");
  printf("\t-r\t- Recursive. Output in a recursive format\n");
  printf("\t-V\t- Version. Display program version\n");
  printf("\t-w\t- Overwrite any output mbox files\n");
  return 0;
}

int version() {
  printf("ReadPST v%s implementing LibPST v%s\n", VERSION, PST_VERSION);
  return 0;
}

char *kmail_chdir = NULL;

char* mk_kmail_dir(char *fname) {
  //change to that directory
  //make a directory based on OUTPUT_KMAIL_DIR_TEMPLATE
  //allocate space for OUTPUT_TEMPLATE and form a char* with fname
  //return that value
  char *dir, *out_name, *index;
  int x;
  if (kmail_chdir != NULL && chdir(kmail_chdir)) {
    x = errno;
    DIE(("mk_kmail_dir: Cannot change to directory %s: %s\n", dir, strerror(x)));
  }
  dir = malloc(strlen(fname)+strlen(OUTPUT_KMAIL_DIR_TEMPLATE)+1);
  sprintf(dir, OUTPUT_KMAIL_DIR_TEMPLATE, fname);
  if (D_MKDIR(dir)) {
    //error occured
    if (errno != EEXIST) {
      x = errno;
      DIE(("mk_kmail_dir: Cannot create directory %s: %s\n", dir, strerror(x)));
    }
  }
  kmail_chdir = realloc(kmail_chdir, strlen(dir)+1);
  strcpy(kmail_chdir, dir);
  free (dir);

  //we should remove any existing indexes created by KMail, cause they might be different now
  index = malloc(strlen(fname)+strlen(KMAIL_INDEX)+1);
  sprintf(index, KMAIL_INDEX, fname);
  unlink(index);
  free(index);

  out_name = malloc(strlen(fname)+strlen(OUTPUT_TEMPLATE)+1);
  sprintf(out_name, OUTPUT_TEMPLATE, fname);
  return out_name;
}

int close_kmail_dir() {
  // change ..
  int x;
  if (kmail_chdir != NULL) { //only free kmail_chdir if not NULL. do not change directory
    free(kmail_chdir);
    kmail_chdir = NULL;
  } else {
    if (chdir("..")) {
      x = errno;
      DIE(("close_kmail_dir: Cannot move up dir (..): %s\n", strerror(x)));
    }
  }
  
  return 0;
}

char* mk_recurse_dir(char *dir) {
  // this will create a directory by that name, then make an mbox file inside that dir.
  // any subsequent dirs will be created by name, and they will contain mbox files
  int x;
  char *out_name;
  if (D_MKDIR (dir)) {
    if (errno != EEXIST) { // not an error because it exists
      x = errno;
      DIE(("mk_recurse_dir: Cannot create directory %s: %s\n", dir, strerror(x)));
    }
  }
  if (chdir (dir)) {
    x = errno;
    DIE(("mk_recurse_dir: Cannot change to directory %s: %s\n", dir, strerror(x)));
  }
  out_name = malloc(strlen("mbox")+1);
  strcpy(out_name, "mbox");
  return out_name;
}

int close_recurse_dir() {
  int x;
  if (chdir("..")) {
    x = errno;
    DIE(("close_recurse_dir: Cannot go up dir (..): %s\n", strerror(x)));
  }
  return 0;
}
// my_stristr varies from strstr in that its searches are case-insensitive
char * my_stristr(char *haystack, char *needle) { 
  char *x=haystack, *y=needle, *z = NULL;

  while (*y != '\0' && *x != '\0') {
    if (tolower(*y) == tolower(*x)) {
      // move y on one
      y++;
      if (z == NULL) {
	z = x; // store first position in haystack where a match is made
      }
    } else {
      y = needle; // reset y to the beginning of the needle
      z = NULL; // reset the haystack storage point
    }
    x++; // advance the search in the haystack
  }
  return z;
}

