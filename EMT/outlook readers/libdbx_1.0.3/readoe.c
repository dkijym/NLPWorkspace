/***************************************************************************
                          readoe.c  -  Open the FOLDERS.DBX file in the 
			  Outlook Express folder and convert all the folders
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
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <time.h>

#include "libdbx.h"
#include "common.h"

#define VERSION "1.0.3"

int usage(int status);
void write_email(FILE *f, char *body);

int main(int argc, char **argv) {
  DBX *dbx, *folders;
  DBXFOLDER *x = NULL;
  DBXEMAIL *z = NULL;
  time_t date;
  int y,a, c, quiet = 0, totalEmails = 0;
  char *fname = NULL;
  FILE *outfile;
  char *indir = NULL, *outdir = NULL;
  
  while ((c = getopt(argc, argv, "hVi:o:q")) != EOF) {
    switch (c) {
    case 'V': //Version
      printf("readoe v%s - Implementing libdbx v%s\n", VERSION, LIBDBX_VERSION);
      exit(0);
    case 'h': //help
      usage(0);
    case 'i': //input directory
      indir = optarg;
      break;
    case 'o': //output directory
      outdir = optarg;
      break;
    case 'q': //quiet
      quiet = 1;
      break;
    default:
      usage(1);
    }
  }

  if (indir == NULL || outdir == NULL) {
    printf("readoe: must specify BOTH input directory and output directory\n");
    usage(2);
  }

  fname = (char*) realloc(fname, strlen(indir)+strlen("Folders.dbx")+2);
  sprintf(fname, "%s/%s", indir, "Folders.dbx");
  folders = dbx_open(fname);
  
  if (folders == NULL){
    dbx_perror("Folder file open");
    return 1;
  }
  
  if (folders->type != DBX_TYPE_FOLDER) {
    printf("File Folders.dbx doesn't appear to contain the correct stuff\n");
    return 2;
  }
  
  for (y = folders->indexCount-1; y >= 0; y--) {
    dbx_free(folders, x);
    x=(DBXFOLDER*)dbx_get(folders, y, 0);
    
    if (dbx_errno != DBX_NOERROR) {
      dbx_perror("Folder Read");
      return 2;
    }
    
    if (x) {
      if (!quiet) {
	printf("\nFolder Name: %s\n", x->name);
      }
    } else
      continue;
    
    if (x->fname != NULL) {
      fname = (char*) realloc (fname, strlen(indir)+strlen(x->fname)+2);
      sprintf(fname, "%s/%s", indir, x->fname);

      dbx = dbx_open(fname);
      if (dbx == NULL) {
	dbx_perror("Email Folder Open");
	continue;
      }
      if (dbx->type != DBX_TYPE_EMAIL) {
	printf("Folder %s doesn't contain emails like I expect\n", fname);
	continue;
      }
      
      fname = (char*) realloc (fname, strlen(outdir)+strlen(x->fname)+2);
      sprintf(fname, "%s/%s", outdir, x->fname);
      
      if ((outfile = fopen (fname, "w")) == NULL) {
	printf("Cannot open output file %s\n", fname);
	continue;
      }
      
      for (a = dbx->indexCount-1; a >= 0; a--) {
	dbx_free(dbx, z);
	z = (DBXEMAIL*)dbx_get(dbx,a, DBX_FLAG_BODY);
	if (dbx_errno != DBX_NOERROR) {
	  dbx_perror("Email Read");
	  break;
	}
	if (z && z->email != NULL) {
	  date = FileTimeToUnixTime(&(z->date), NULL);
	  fprintf(outfile, "From %s %s", z->sender_address, ctime(&date)); //ctime adds \n
	  write_email(outfile, z->email);
	  fprintf(outfile, "\n\n");
	} else if (z == NULL) {
	  printf("DBX returned a NULL email\n");
	} else {
	  if (!quiet)
	    printf("Email has no body\n");
	}
      }
      if (!quiet)
	printf("%d emails processed\n", dbx->indexCount+1);
      totalEmails += (dbx->indexCount+1);
      fclose(outfile);
      dbx_close(dbx);
    }
  }

  if (!quiet)
    printf("Total Emails processed: %d\n", totalEmails);

  dbx_close(folders);
  return 0;
}

int usage(int status) {
  printf("readoe - Extract emails from MS Outlook Express 5.0 directory into mbox format files\n");
  printf("Usage: readoe [OPTIONS] -i INDIR -o OUTDIR\n");
  printf("Options:\n");
  printf("\t-h              display this help and exit\n");
  printf("\t-V              output version information and exit\n");
  printf("\t-i \"indir\"      input directory containing \"Folders.dbx\"\n");
  printf("\t-o \"outdir\"     directory to put converted files. Files will be named the \n");
  printf("\t                same as the input ones. eg. Inbox.dbx\n");
  printf("\t-q              display no extra information whilst processing\n");
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
