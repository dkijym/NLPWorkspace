/***************************************************************************
                          readdbx.c  -  Read and dump contents of a DBX file
                             -------------------
    begin                : April 2001
    copyright            : (C) 2001 by David Smith
    email                : Dave.S@Earthcorp.Com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include <stdio.h>
#include <getopt.h>
#include <time.h>
#include <string.h>

#include "libdbx.h"
#include "common.h"

#define VERSION "1.0.3"

int usage(int status);
void write_email(FILE *f, char *body);

int main(int argc, char ** argv) {
  DBX *dbx;
  DBXEMAIL *email;
  time_t date;

  void *blob = NULL;
  int y,x = 0;
  int c, quiet = 0;
  char *infile = NULL;
  char *outfile = NULL;
  FILE *out = NULL;

  while ((c = getopt(argc, argv, "hVf:o:q" )) != EOF) {
    switch (c) {
    case 'V': //Version
      printf("readdbx v%s - Implementing libdbx v%s\n", VERSION, LIBDBX_VERSION);
      exit(0);
    case 'h': //help
      usage(0);
    case 'f': //infile
      infile = optarg;
      break;
    case 'o': //outfile
      outfile = optarg;
      break;
    case 'q': //Quiet
      quiet = 1;
      break;
    default:
      usage(1);
    }
  }

  if (infile != NULL) {
    dbx = dbx_open(infile);
  } else {
    dbx = dbx_open_stream(stdin);
  }

  if (outfile != NULL) {
    if ((out = fopen(outfile, "w")) == NULL) {
      fprintf(stderr, "readdbx: cannot open output file \"%s\"\n", outfile);
      exit(2);
    }
  } else {
    out = stdout;
  }

  if (dbx == NULL) {
    dbx_perror("dbx_open");
    return 1;
  }

  for (y = dbx->indexCount-1; y >= 0; y--) {
    dbx_free(dbx, blob);
    blob = dbx_get(dbx, y, DBX_FLAG_BODY);

    if (dbx_errno != DBX_NOERROR) {
      dbx_perror("dbx_get");
      return 3;
    }

    if (blob) {
      if (dbx->type == DBX_TYPE_FOLDER) {
	if (!quiet) {
	  fprintf(stderr, "Folder Name: %s\nFolder file: %s\nFolder ID: %#X\nFolder Parent: %#X\n\n", 
		  ((DBXFOLDER*)blob)->name, 
		  ((DBXFOLDER*)blob)->fname,
		  ((DBXFOLDER*)blob)->id,
		  ((DBXFOLDER*)blob)->parentid);
	}
      } else if (dbx->type == DBX_TYPE_EMAIL) {
	email = blob;
	if (!quiet) {
	  fprintf(stderr, "Subject: %s\nSender: %s\n\taddress: %s\nRecip: %s\n\taddress: %s\nMessage ID: %s\nParent ID(s): %s\n", 
		  email->subject,  
		  email->sender_name, 
		  email->sender_address,
		  email->recip_name,
		  email->recip_address,
		  email->messageid,
		  email->parent_message_ids);
	  fprintf(stderr, "Id: %d\n\n",
		  email->id);
	}

	date = FileTimeToUnixTime(&(email->date), NULL);
	fprintf(out, "From %s %s", email->sender_address, ctime(&date)); //ctime adds \n
	write_email(out, email->email);
	fprintf(out, "\n\n");
      } else {
	fprintf(stderr, "Unknown object type\n");
      }
    } else {
      fprintf(stderr, "dbx_get returned a NULL\n");
    }
  }
}
       
int usage (int status) {
  printf("readdbx - Extract emails from MS Outlook Express 5.0 DBX files into mbox format.\n");
  printf("File is taken from stdin unless -f is specified.\n");
  printf("Output emails are written to stdout unless -o is specified\n\n");
  printf("Usage: readdbx [OPTIONS]\n");
  printf("Options:\n");
  printf("\t-h           display this help and exit\n");
  printf("\t-V           output version information and exit\n");
  printf("\t-f \"file\"    input DBX file\n");
  printf("\t-o \"file\"    file to write mbox format to\n");
  printf("\t-q           don't display extra information\n");
  printf("\n");
  exit(status);
}

void write_email(FILE *f, char *body) {
  char *n = body;
  while (n != NULL) {
    if (strncmp(body, "From ", 5) == 0)
      fprintf(f, ">");
    if ((n = strchr(body, '\n'))) {
      n++;
      fwrite(body, n-body+1, 1, f); //write just a line
      body = n+1;
    }
  }
  fwrite(body, strlen(body), 1, f);
}
