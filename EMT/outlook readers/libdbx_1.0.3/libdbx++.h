/***************************************************************************
                          libdbx++.h  -  description
                             -------------------
    begin                : Thu Jul 12 2001
    copyright            : (C) 2001 by Dave Smith
    email                : dave.s@earthcorp.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#ifndef __LIBDBXPP_H
#define __LIBDBXPP_H

#include "libdbx.h"

class CItem;
class CEmail;
class CFolder;

class CDBX {
private:
	DBX *dbx;
	int itemCount;
public:
	CDBX();
	~CDBX();
	int open(const char *fname);
	int open(FILE *fp);
	void close();
	CItem* get(int index, int flags);
	CItem* next(int flags);
	void reset();
	int type();
	void getBody(int start_ptr, char** ptr);
	CEmail* getBody(CEmail *email);
	int indexCount() {return dbx->indexCount;};
	const char* perror() {return dbx_strerror(dbx_errno);}
};

class CItem {
	void *data;
public:
	CItem();
	virtual ~CItem();
	int type;
	void set(void*);
	CEmail *toEmail();
	CFolder *toFolder();
	int structOK() {return data != NULL;};
};

class CEmail : public CItem {
	friend class CDBX;
	DBXEMAIL *email;
	DBXEMAIL *getStruct();
public:
	CEmail();
	virtual ~CEmail();
	void set(void*);
	void clearBody();
	int structOK() {return email != NULL;};
	
	int id()                   {return email==NULL?-1:email->id;};
	FILETIME *date()						 {return email==NULL?NULL:&email->date;};
	char *body()               {return email==NULL?NULL:email->email;};
	char *messageid()          {return email==NULL?NULL:email->messageid;};
	char *parent_message_ids() {return email==NULL?NULL:email->parent_message_ids;};
	char *psubject()           {return email==NULL?NULL:email->psubject;};
	char *recip_address()      {return email==NULL?NULL:email->recip_address;};
	char *recip_name()         {return email==NULL?NULL:email->recip_name;};
	char *sender_address()     {return email==NULL?NULL:email->sender_address;};
	char *sender_name()        {return email==NULL?NULL:email->sender_name;};
	char *subject()            {return email==NULL?NULL:email->subject;};
	int isSeen();
};

class CFolder : public CItem {
	DBXFOLDER *folder;
	DBXFOLDER* getStruct();
public:
	CFolder();
	virtual ~CFolder();
	CFolder *next;
	CFolder *child;
	void set(void*);
	int structOK() {return folder != NULL;};
	
	int id()       {return folder==NULL?-1:folder->id;};
	char *fname()  {return folder==NULL?NULL:folder->fname;};
	char *name()   {return folder==NULL?NULL:folder->name;};
	int parentid() {return folder==NULL?-1:folder->parentid;};
	void fname(const char *f);
	void name(const char *n);
};

#endif
