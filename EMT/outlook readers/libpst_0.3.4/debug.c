/* Contains the debug functions */
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <ctype.h>

void _pst_debug(char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  vfprintf(stderr, fmt, ap);
  va_end(ap);
}

#define NUM_COL 30
void _pst_debug_hexdump(FILE *out, char *buf, int size, int col) {
  int off = 0, toff;
  int count = 0;
  
  if (col == -1) {
    col = NUM_COL;
  }

  while (off < size) {
    fprintf(out, "%X\t:", off);
    toff = off;
    while (count < col && off < size) {
      fprintf(out, "%02hhx ", buf[off]);
      off++; count++;
    }
    off = toff;
    while (count < col) {
      // only happens at end of block to pad the text over to the text column
      fprintf(out, "   ");
      count++;
    }
    count = 0;

    while (count < col && off < size) {
      fprintf(out, "%c", isgraph(buf[off])?buf[off]:' ');
      off++; count ++;
    }

    fprintf(out, "\n");
    count=0;
  }

  fprintf(out, "\n");
}

void _pst_debug_hexprint(char *data, int size) {
  int i = 0;
  while (i < size) {
    fprintf(stderr, "%02hhX", data[i]);
    i++;
  }
}
