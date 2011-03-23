/***************************************************************************
                          libdbx++.cpp  -  description
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

#include "libdbx.h"
#include "libdbx++.h"
//#include "classes.h"

CDBX::CDBX() {
	dbx = NULL;
	itemCount = 0;
}

CDBX::~CDBX() {
	if (dbx != NULL) {
		close();
	}
}

int CDBX::open(const char* fname) {
	if (dbx != NULL) {
		close();
	}

	if ((dbx = dbx_open(fname)) == NULL) {
		return 1;
	}
	itemCount = 0;
	return 0;
}

int CDBX::open(FILE * stream) {
	if (dbx != NULL) {
		close();
	}
	
	if ((dbx = dbx_open_stream(stream)) == NULL) {
		return 1;
	}
	itemCount = 0;
	return 0;
}	
	
void CDBX::close() {
	if (dbx != NULL) {
		dbx_close(dbx);
		dbx = NULL;
	}
}

CItem* CDBX::get(int index, int flags) {
	CItem* item;
	DBXEMAIL *email;
	
	email = (DBXEMAIL*) dbx_get(dbx, index, flags);
	if (email->type == DBX_TYPE_FOLDER) {
		item = new CFolder();
		((CFolder*)item)->set((void*)email);
	}	else {
		item = new CEmail();
		((CEmail*)item)->set ((void*)email);
	}
	
	return item;
}

CItem* CDBX::next(int flags) {
	if (itemCount >= dbx->indexCount)
		return NULL;
	return get(itemCount++, flags);
}

void CDBX::reset() {
	itemCount = 0;
}

int CDBX::type() {
	return dbx->type;
}

void CDBX::getBody(int start_ptr, char **ptr) {
	dbx_get_body(dbx, start_ptr, ptr);
}

CEmail *CDBX::getBody(CEmail *email) {
	if (email->type != DBX_TYPE_EMAIL) {
		return NULL;
	}
	
	dbx_get_email_body(dbx, email->getStruct());
	return email;
}



/*** CItem Class - Base class for CEmail and CFolder ***/

/* Constructor */
CItem::CItem() {
	type = DBX_TYPE_VOID;
	data = NULL;
}

/* Destructor */
CItem::~CItem() {
	if (data != NULL)
		dbx_free_item(data);
}

/* Cast self into a CEmail object */
CEmail* CItem::toEmail() {
	return (CEmail*) this;
}

/* Case self into a CFolder object */
CFolder* CItem::toFolder() {
	return (CFolder*) this;
}

void CItem::set(void *dat) {
	if (data != NULL)
		dbx_free_item(data);
	data = dat;
}

/*** CEmail - Inherits CItem ***/

/* Constructor */
CEmail::CEmail() {
	email = NULL;
	type = DBX_TYPE_EMAIL;
}

/* Destructor */
CEmail::~CEmail() {
	if (email != NULL)
		dbx_free_item(email);
}

/* Returns a pointer to the structure */
DBXEMAIL * CEmail::getStruct() {
	return email;
}

void CEmail::set(void* new_email) {
	if (email != NULL) {
		dbx_free_item(email);
	}
	email = (DBXEMAIL*) new_email;
}

void CEmail::clearBody() {
	if (email != NULL) {
		dbx_free_email_body(email);
	}
}

/*** CFolder - Inherits CItem ***/
CFolder::CFolder() {
	folder = NULL;
	type = DBX_TYPE_FOLDER;
}

CFolder::~CFolder() {
	if (folder != NULL)
		dbx_free_item(folder);
}

DBXFOLDER * CFolder::getStruct() {
	return folder;
}
	
void CFolder::set(void *new_fol) {
	if (folder != NULL) {
		dbx_free_item(folder);
	}
	folder = (DBXFOLDER*) new_fol;
}

