#ifndef __TIMECONV_H
#define __TIMECONV_H

#include "common.h"

#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif
  time_t FileTimeToUnixTime( const FILETIME *filetime, DWORD *remainder );

  char * FileTimeToAscii (const FILETIME *filetime);
  
#ifdef __cplusplus
}
#endif

#endif
