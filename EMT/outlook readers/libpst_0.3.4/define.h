/***************************************************************************
                          define.h  -  Contains my debug macros
                             -------------------
    begin                : January 2001
    copyright            : (C) 2001 by David Smith
    email                : Dave.S@Earthcorp.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

//#define DEBUG_ALL
#ifndef DEFINEH_H
#define DEFINEH_H

//variable number of arguments to this macro. will expand them into 
// ## args, then exit with status of 1
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

#ifdef __LINUX__
#include <netinet/in.h>
#include <unistd.h>
#endif


void _pst_debug(char *fmt, ...);
void _pst_debug_hexdump(FILE* out, char* buf, int size, int col);
void _pst_debug_hexprint(char *data, int size);

#ifdef DEBUGSAVE
#define MESSAGEINIT(filename) messagefp=fopen(filename, "w");
#define MESSAGESAVE(format, args...) messagefp!=NULL?fprintf(messagefp, format, ##args):0;
#define MESSAGESTOP() fclose(messagefp);

#define DEBUGINIT(filename) debugfp=fopen(filename, "w"); //open with truncate
#define DEBUGWRITE(format,args...) debugfp!=NULL?fprintf(debugfp, format, ##args):0;
#define DEBUGSTOP() fclose(debugfp);
#else
#define MESSAGEINIT(filename)
#define MESSAGESAVE(format, args...) {}
#define MESSAGESTOP() {}
#define DEBUGINIT(a...) {}
#define DEBUGWRITE(format,args...) {}
#define DEBUGSTOP() {}
#endif


#ifdef DEBUG_ALL
#define MESSAGEPRINT(x) _pst_debug x;
#else
#define MESSAGEPRINT(x)
#endif

#define LOGSTOP() {MESSAGESTOP();DEBUGSTOP();}

#define DIE(format,args...) {\
 MESSAGEPRINT(format, ##args);\
 MESSAGESAVE(format, ##args);\
 LOGSTOP();\
 exit(1);\
}
#define WARN(x) MESSAGEPRINT(x);

#ifdef DEBUG_ALL
#define DEBUG_MODE_GEN
#define DEBUGPRINT
#define DEBUG_MODE_WARN
//#define DEBUG_MODE_READ
#define DEBUG_MODE_EMAIL
#define DEBUG_MODE_MAIN
//#define DEBUG_MODE_INDEX
#define DEBUG_MODE_CODE
#define DEBUG_MODE_INFO
#define DEBUG_MODE_HEXDUMP
//#define DEBUG_MODE_DECRYPT
//#define DEBUGSAVE
//extern FILE *debugfp;
#endif

//extern FILE *messagefp;

#ifdef DEBUGPRINT
#define DEBUG_PRINT(x) _pst_debug x;
#else
#define DEBUG_PRINT(x)
#endif

#ifdef DEBUG_MODE_GEN
#define DEBUG(x) {DEBUG_PRINT(x);}
#else
#define DEBUG(x) {}
#endif

#ifdef DEBUG_MODE_INDEX
#define DEBUG_INDEX(x) DEBUG(x);
#else
#define DEBUG_INDEX(x) {}
#endif

#ifdef DEBUG_MODE_EMAIL
#define DEBUG_EMAIL(x) DEBUG(x);
#define DEBUG_EMAIL_HEXPRINT(x,y) _pst_debug_hexprint(x, y);
#else
#define DEBUG_EMAIL(x) {}
#define DEBUG_EMAIL_HEXPRINT(x,y) {}
#endif

#ifdef DEBUG_MODE_WARN
#define DEBUG_WARN(x) DEBUG(x);
#else
#define DEBUG_WARN(x) {}
#endif

#ifdef DEBUG_MODE_READ
#define DEBUG_READ(x) DEBUG(x);
#else
#define DEBUG_READ(x) {}
#endif

#ifdef DEBUG_MODE_INFO
#define DEBUG_INFO(x) DEBUG(x);
#else
#define DEBUG_INFO(x) {}
#endif

#ifdef DEBUG_MODE_MAIN
#define DEBUG_MAIN(x) DEBUG(x);
#else
#define DEBUG_MAIN(x) {}
#endif

#ifdef DEBUG_MODE_CODE
#define DEBUG_CODE(x) {x}
#else
#define DEBUG_CODE(x) {}
#endif

#ifdef DEBUG_MODE_DECRYPT
#define DEBUG_DECRYPT(x) DEBUG(x);
#else
#define DEBUG_DECRYPT(x) {}
#endif

#ifdef DEBUG_MODE_HEXDUMP
#define DEBUG_HEXDUMP(x, s) _pst_debug_hexdump(stderr, x, s, 30);
#define DEBUG_HEXDUMPC(x, s, c) _pst_debug_hexdump(stderr, x, s, c);
#else
#define DEBUG_HEXDUMP(x, s) {}
#define DEBUG_HEXDUMPC(x, s, c) {}
#endif

#define RET_DERROR(res, ret_val, x)\
	if (res) { DEBUG_WARN(x); return ret_val; }

#define RET_ERROR(res, ret_val)\
	if (res) {return ret_val;}	

#endif //DEFINEH_H





