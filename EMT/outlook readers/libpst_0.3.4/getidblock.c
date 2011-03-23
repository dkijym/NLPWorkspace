#include <stdio.h>
#include "libpst.h"
#include "define.h"

int main(int argc, char ** argv) {
  // pass the id number to display on the command line
  char *fname, *sid;
  pst_file pstfile;
  int id;
  pst_index_ll *ptr;
  char *buf = NULL;

  if (argc < 3) {
    DIE(("getidblock pst_file id_no\n"));
  }
  
  fname = argv[1];
  sid = argv[2];

  id = strtol(sid, NULL, 0);
  DEBUG_MAIN(("Opening file\n"));
  if (pst_open(&pstfile, fname, "r")) {
    DIE(("Error opening file\n"));
  }
  
  DEBUG_MAIN(("Loading Index\n"));
  pst_load_index(&pstfile);

  if ((ptr = _pst_getID(&pstfile, id)) == NULL) {
    DIE(("id not found [%#x]\n", id));
  }

  DEBUG_MAIN(("Loading block\n"));
  if (_pst_read_block_size(&pstfile, ptr->offset, ptr->size, &buf, 1, 1) == 0) {
    DIE(("Error loading block\n"));
  }
  DEBUG_MAIN(("Printing block...\n"));
  _pst_debug_hexdump(stdout, buf, ptr->size, 0x16);
  free(buf);
  pst_close(&pstfile);
  return 0;
}
