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

#include "define.h"

#ifndef _MSC_VER
#include "timeconv.h"
#include <unistd.h>

#include <endian.h>
#include <byteswap.h>

# if __BYTE_ORDER == __BIG_ENDIAN
#  define LE32_CPU(x) x = bswap_32(x);
#  define LE16_CPU(x) x = bswap_16(x);
# elif __BYTE_ORDER == __LITTLE_ENDIAN
#  define LE32_CPU(x) {}
#  define LE16_CPU(x) {}
# else
#  error "Byte order not supported by this library"
# endif

#endif //ifdef _MSC_VER

#include "libpst.h"

#ifdef _MSC_VER
#include "windows.h"
#define WARN printf
#define DEBUG_INFO printf
#define DEBUG_EMAIL printf
#define DEBUG_READ printf
#define DEBUG_DECRYPT printf
#define DEBUG_CODE printf
#define DEBUG_INDEX printf
#define DEBUG_WARN printf
#define DEBUG printf

#define LE32_CPU(x) {}
#define LE16_CPU(x) {}
#endif // _MSC_VER

#define FILE_SIZE_POINTER 0xA8
#define INDEX_POINTER 0xC4
#define SECOND_POINTER 0xBC
#define INDEX_DEPTH 0x4C
#define SECOND_DEPTH 0x5C
// the encryption setting could be at 0x1CC. Will require field testing
#define ENC_OFFSET 0x1CD

#define PST_SIGNATURE 0x4E444221

struct _pst_table_ptr_struct{
  int start;
  int u1;
  int offset;
};

typedef struct _pst_block_header {
  short int type;
  short int count;
} pst_block_header;

typedef struct _pst_id2_assoc {
  int id2;
  int id;
  int table2;
} pst_id2_assoc;

// this is an array of the un-encrypted values. the un-encrypyed value is in the position 
// of the encrypted value. ie the encrypted value 0x13 represents 0x02
//                     0     1     2     3     4     5     6     7 
//                     8     9     a     b     c     d     e     f 
unsigned char comp_enc [] = { 0x47, 0xf1, 0xb4, 0xe6, 0x0b, 0x6a, 0x72, 0x48,
                    0x85, 0x4e, 0x9e, 0xeb, 0xe2, 0xf8, 0x94, 0x53, /*0x0f*/ 
		    0xe0, 0xbb, 0xa0, 0x02, 0xe8, 0x5a, 0x09, 0xab,
                    0xdb, 0xe3, 0xba, 0xc6, 0x7c, 0xc3, 0x10, 0xdd, /*0x1f*/ 
		    0x39, 0x05, 0x96, 0x30, 0xf5, 0x37, 0x60, 0x82,
                    0x8c, 0xc9, 0x13, 0x4a, 0x6b, 0x1d, 0xf3, 0xfb, /*0x2f*/ 
		    0x8f, 0x26, 0x97, 0xca, 0x91, 0x17, 0x01, 0xc4,
                    0x32, 0x2d, 0x6e, 0x31, 0x95, 0xff, 0xd9, 0x23, /*0x3f*/ 
		    0xd1, 0x00, 0x5e, 0x79, 0xdc, 0x44, 0x3b, 0x1a,
		    0x28, 0xc5, 0x61, 0x57, 0x20, 0x90, 0x3d, 0x83, /*0x4f*/ 
		    0xb9, 0x43, 0xbe, 0x67, 0xd2, 0x46, 0x42, 0x76,
		    0xc0, 0x6d, 0x5b, 0x7e, 0xb2, 0x0f, 0x16, 0x29, /*0x5f*/
		    0x3c, 0xa9, 0x03, 0x54, 0x0d, 0xda, 0x5d, 0xdf,
		    0xf6, 0xb7, 0xc7, 0x62, 0xcd, 0x8d, 0x06, 0xd3, /*0x6f*/
		    0x69, 0x5c, 0x86, 0xd6, 0x14, 0xf7, 0xa5, 0x66,
		    0x75, 0xac, 0xb1, 0xe9, 0x45, 0x21, 0x70, 0x0c, /*0x7f*/
		    0x87, 0x9f, 0x74, 0xa4, 0x22, 0x4c, 0x6f, 0xbf,
		    0x1f, 0x56, 0xaa, 0x2e, 0xb3, 0x78, 0x33, 0x50, /*0x8f*/
		    0xb0, 0xa3, 0x92, 0xbc, 0xcf, 0x19, 0x1c, 0xa7,
		    0x63, 0xcb, 0x1e, 0x4d, 0x3e, 0x4b, 0x1b, 0x9b, /*0x9f*/
		    0x4f, 0xe7, 0xf0, 0xee, 0xad, 0x3a, 0xb5, 0x59,
		    0x04, 0xea, 0x40, 0x55, 0x25, 0x51, 0xe5, 0x7a, /*0xaf*/
		    0x89, 0x38, 0x68, 0x52, 0x7b, 0xfc, 0x27, 0xae,
		    0xd7, 0xbd, 0xfa, 0x07, 0xf4, 0xcc, 0x8e, 0x5f, /*0xbf*/
		    0xef, 0x35, 0x9c, 0x84, 0x2b, 0x15, 0xd5, 0x77,
		    0x34, 0x49, 0xb6, 0x12, 0x0a, 0x7f, 0x71, 0x88, /*0xcf*/
		    0xfd, 0x9d, 0x18, 0x41, 0x7d, 0x93, 0xd8, 0x58,
		    0x2c, 0xce, 0xfe, 0x24, 0xaf, 0xde, 0xb8, 0x36, /*0xdf*/
		    0xc8, 0xa1, 0x80, 0xa6, 0x99, 0x98, 0xa8, 0x2f,
		    0x0e, 0x81, 0x65, 0x73, 0xe4, 0xc2, 0xa2, 0x8a, /*0xef*/
		    0xd4, 0xe1, 0x11, 0xd0, 0x08, 0x8b, 0x2a, 0xf2,
		    0xed, 0x9a, 0x64, 0x3f, 0xc1, 0x6c, 0xf9, 0xec}; /*0xff*/

int pst_open(pst_file *pf, char *name, char *mode) {
  unsigned int sig;

  if (pf == NULL) {
    WARN (("pst_open: cannot be passed a NULL pst_file\n"));
    return -1;
  }
  memset(pf, 0, sizeof(pst_file));
  
  if ((pf->fp = fopen(name, mode)) == NULL) {
    WARN(("pst_open: cannot open PST file. Error\n"));
    return -1;
  }
  if (fread(&sig, sizeof(sig), 1, pf->fp) == 0) {
    fclose(pf->fp);
    WARN(("pst_open: cannot read signature from PST file. Closing on error\n"));
    return -1;
  }

  // architecture independant byte-swapping (little, big, pdp)
  LE32_CPU(sig);

  DEBUG_INFO(("sig = %X\n", sig));
  if (sig != PST_SIGNATURE) {
    fclose(pf->fp);
    WARN(("pst_open: not a PST file that I know. Closing with error\n"));
    return -1;
  }
  
  _pst_getAtPos(pf->fp, ENC_OFFSET, &(pf->encryption), sizeof(unsigned char));
  DEBUG_INFO(("encrypt = %i\n", pf->encryption));
  //  pf->encryption = encrypt;
    
  _pst_getAtPos(pf->fp, SECOND_POINTER-4, &(pf->index2_count), sizeof(pf->index2_count));
  _pst_getAtPos(pf->fp, SECOND_POINTER, &(pf->index2), sizeof(pf->index2));
  LE32_CPU(pf->index2_count);
  LE32_CPU(pf->index2);

  _pst_getAtPos(pf->fp, FILE_SIZE_POINTER, &(pf->size), sizeof(pf->size));
  LE32_CPU(pf->size);

  // very tempting to leave these values set way too high and let the exploration of the tables set them...
  pf->index1_depth = pf->index2_depth = 255;

  DEBUG_INFO(("pst_open: Pointer2 is %#X, count %i[%#x], depth %#x\n", 
    pf->index2, pf->index2_count, pf->index2_count, pf->index2_depth));
  _pst_getAtPos(pf->fp, INDEX_POINTER-4, &(pf->index1_count), sizeof(pf->index1_count));
  _pst_getAtPos(pf->fp, INDEX_POINTER, &(pf->index1), sizeof(pf->index1));
  LE32_CPU(pf->index1_count);
  LE32_CPU(pf->index1);

  DEBUG_INFO(("pst_open: Pointer1 is %#X, count %i[%#x], depth %#x\n", 
    pf->index1, pf->index1_count, pf->index1_count, pf->index1_depth));
  
  return 0;
}

int pst_close(pst_file *pf) {
  if (pf->fp == NULL) {
    WARN(("pst_close: cannot close NULL fp\n"));
    return -1;
  }
  if (fclose(pf->fp)) {
    WARN(("pst_close: fclose returned non-zero value\n"));
    return -1;
  }
  // we must free the id linklist and the desc tree
  _pst_free_id (pf->i_head);
  _pst_free_desc (pf->d_head);
  return 0;
}

int pst_load_index (pst_file *pf) {
  int x,y;
  
  if (pf == NULL) {
    WARN(("pst_load_index: Cannot load index for a NULL pst_file\n"));
    return -1;
  }

  x = _pst_build_id_ptr(pf, pf->index1, 0, 0x4, INT_MAX);
  if (x == -1 || x == 4) {
    if (x == -1) 
      pf->index1_depth = 0; //only do this for -1

    if (_pst_build_id_ptr(pf, pf->index1, 0, 0x4, INT_MAX) == -1) {
      //we must call twice for testing the depth of the index
      return -1;
    }
  }

  DEBUG_INDEX(("pst_load_index: Second Table\n"));
  y = -1;  
  x = _pst_build_desc_ptr(pf, pf->index2, 0, &y, 0x21, INT_MAX);
  if (x == -1 || x == 4) {
    if (x == -1)
      pf->index2_depth = 0; //only if -1 is return val

    if (_pst_build_desc_ptr(pf, pf->index2, 0, &y, 0x21, INT_MAX) == -1) {
      // we must call twice for testing the depth of the index
      return -1;
    }
  }

  DEBUG_CODE(_pst_printDptr(pf);)
  return 0;
}

int id_depth_ok = 0;
#define BLOCK_SIZE 516

int _pst_build_id_ptr(pst_file *pf, int offset, int depth, int start_val, int end_val) {
  struct _pst_table_ptr_struct table, table2;
  pst_index_ll *i_ptr=NULL;
  pst_index index;
  //  int fpos = ftell(pf->fp);
  int x, ret;
  char *buf = NULL, *bptr = NULL;

  if (pf->index1_depth - depth == 0) {
    // we must be at a leaf table. These are index items
    DEBUG_INDEX(("_pst_build_id_ptr: Reading Items\n"));
    //    fseek(pf->fp, offset, SEEK_SET);
    x = 0;

    _pst_read_block_size(pf, offset, BLOCK_SIZE, &buf, 0, 0);
    bptr = buf;
    //    DEBUG_HEXDUMP(buf, BLOCK_SIZE);
    
    memcpy(&index, bptr, sizeof(index));
    LE32_CPU(index.id);
    LE32_CPU(index.offset);
    LE16_CPU(index.size);
    LE16_CPU(index.u1);
    bptr += sizeof(index);

    while(index.id != 0 && x < 42 && bptr < buf+BLOCK_SIZE && index.id < end_val) {
      DEBUG_INDEX(("_pst_build_id_ptr: [%i]%i Item [id = %#x, offset = %#x, u1 = %#x, size = %i(%#x)]\n", depth, ++x, index.id, index.offset, index.u1, index.size, index.size));
      if (start_val != -1 && index.id != start_val) {
	DEBUG_WARN(("_pst_build_id_ptr: This item isn't right. Must be corruption, or I got it wrong!\n"));
	//	fseek(pf->fp, fpos, SEEK_SET);
	if (buf) free(buf);
	return -1;
      } else {
	start_val = -1;
	id_depth_ok = 1;
      }
      // u1 could be a flag. if bit 0x2 is not set, it might be deleted
      //      if (index.u1 & 0x2 || index.u1 & 0x4) { 
      // ignore the above condition. it doesn't appear to hold
      i_ptr = (pst_index_ll*) xmalloc(sizeof(pst_index_ll));
      i_ptr->id = index.id;
      i_ptr->offset = index.offset;    	
      i_ptr->u1 = index.u1;
      i_ptr->size = index.size;
      i_ptr->next = NULL;
      if (pf->i_tail != NULL)
	pf->i_tail->next = i_ptr;
      if (pf->i_head == NULL)
	pf->i_head = i_ptr;
      pf->i_tail = i_ptr;
      /*    } else {
	DEBUG_INDEX(("_pst_build_id_ptr:     Item is isn't valid. It has been deleted\n"));
	}*/
      memcpy(&index, bptr, sizeof(index));
      LE32_CPU(index.id);
      LE32_CPU(index.offset);
      LE16_CPU(index.size);
      LE16_CPU(index.u1);
      bptr += sizeof(index);
    }
    //    fseek(pf->fp, fpos, SEEK_SET);
    if (buf) free (buf);
    return 2;      
  } else {
    // this is then probably a table of offsets to more tables.
    DEBUG_INDEX(("_pst_build_id_ptr: Reading Table Items\n"));

    //    fseek(pf->fp, offset, SEEK_SET);
    x = 0;
    ret = 0;

    _pst_read_block_size(pf, offset, BLOCK_SIZE, &buf, 0, 0);
    bptr = buf;
    //    DEBUG_HEXDUMP(buf, BLOCK_SIZE);

    memcpy(&table, bptr, sizeof(table));
    LE32_CPU(table.start);
    LE32_CPU(table.u1);
    LE32_CPU(table.offset);
    bptr += sizeof(table);
    memcpy(&table2, bptr, sizeof(table));
    LE32_CPU(table2.start);
    LE32_CPU(table2.u1);
    LE32_CPU(table2.offset);

    if (start_val != -1 && table.start != start_val) {
      DEBUG_WARN(("_pst_build_id_ptr: This table isn't right. Must be corruption, or I got it wrong!\n"));
      //	fseek(pf->fp, fpos, SEEK_SET);
      if (buf) free(buf);
      return -1;
    } 

    while (table.start != 0 && bptr < buf+BLOCK_SIZE && table.start < end_val) {
      DEBUG_INDEX(("_pst_build_id_ptr: [%i] %i Table [start id = %#x, u1 = %#x, offset = %#x]\n", depth, ++x, table.start, table.u1, table.offset));

      if (table2.start < table.start) 
	// this should only be the case when we come to the end of the table
	// and table2.start == 0
	table2.start = end_val;

      if ((ret = _pst_build_id_ptr(pf, table.offset, depth+1, table.start, table2.start)) == -1 && id_depth_ok == 0) {
	// it would appear that if the table below us isn't a table, but data, then we are actually the table. hmmm
	DEBUG_INDEX(("_pst_build_id_ptr: Setting max depth to %i\n", depth));
	pf->index1_depth = depth; //set max depth to this level
	if (buf) free (buf);
	//	fseek(pf->fp, fpos, SEEK_SET);
	return 4; // this will indicate that we want to be called again with the same parameters
      } else if (ret == 4) {
	//we shan't bother with checking return value?
	DEBUG_INDEX(("_pst_build_id_ptr: Seen that a max depth has been set. Calling build again\n"));
	_pst_build_id_ptr(pf, table.offset, depth+1, table.start, table2.start);
      } else if (ret == 2) {
	DEBUG_INDEX(("_pst_build_id_ptr: child returned successfully\n"));
      } else {
	DEBUG_INDEX(("_pst_build_id_ptr: child has returned without a known error [%i]\n", ret));
      }
      memcpy(&table, bptr, sizeof(table));
      LE32_CPU(table.start);
      LE32_CPU(table.u1);
      LE32_CPU(table.offset);
      bptr += sizeof(table);
      memcpy(&table2, bptr, sizeof(table));
      LE32_CPU(table2.start);
      LE32_CPU(table2.u1);
      LE32_CPU(table2.offset);
    }
    if (table.start == 0) {
      DEBUG_INDEX(("_pst_build_id_ptr: Table.start == 0\n"));
    }

    //    fseek(pf->fp, fpos, SEEK_SET);
    if (ret != 0) {
      if (buf) free(buf);
      DEBUG_INDEX(("_pst_build_id_ptr: RET == %i [%#x]\n", ret, ret));
      return -1;
    }
    if (buf) free (buf);
    return 3;
  }
  DEBUG(("_pst_build_id_ptr: ERROR ** Shouldn't be here!\n"));
  //  fseek(pf->fp, fpos, SEEK_SET);
  return 1;
}

int desc_depth_ok = 0;
#define DESC_BLOCK_SIZE 520
int _pst_build_desc_ptr (pst_file *pf, int offset, int depth, int *high_id, int start_id, int end_val) {
  struct _pst_table_ptr_struct table, table2;
  pst_desc desc_rec;
  pst_desc_ll *d_ptr=NULL, *d_par=NULL;
  
  int i = 0, y;
  char *buf = NULL, *bptr;

  struct _pst_d_ptr_ll {
    pst_desc_ll * ptr;
    struct _pst_d_ptr_ll * next;
    struct _pst_d_ptr_ll * prev;
  } *d_ptr_head=NULL, *d_ptr_tail=NULL, *d_ptr_ptr=NULL;

  int d_ptr_count = 0;

  if (pf->index2_depth-depth == 0) {
    // leaf node
    _pst_read_block_size(pf, offset, DESC_BLOCK_SIZE, &buf, 0, 0);
    bptr = buf;
    //    DEBUG_HEXDUMPC(buf, DESC_BLOCK_SIZE, 16);

    memcpy(&desc_rec, bptr, sizeof(desc_rec));
    LE32_CPU(desc_rec.d_id);
    LE32_CPU(desc_rec.desc_id);
    LE32_CPU(desc_rec.list_id);
    LE32_CPU(parent_id);
    bptr+= sizeof(desc_rec);

    while (i < 0x1F && desc_rec.d_id < end_val) {
      DEBUG_INDEX(("pst_load_index: [%i] Item(%#x) = [d_id = %#x, desc_id = %#x, "
		  "list_id = %#x, parent_id = %#x]\n", depth, i, desc_rec.d_id, 
		  desc_rec.desc_id, desc_rec.list_id, desc_rec.parent_id));
      i++;

      if (start_id != -1 && desc_rec.d_id != start_id) {
	DEBUG_INDEX(("pst_load_index: Error: This table appears to be corrupt. Perhaps"
		    " we are looking too deep!\n"));
	if (buf) free(buf);
	return -1;
      } else {
	start_id = -1;
	desc_depth_ok = 1;
      }

      if (desc_rec.d_id == 0) {
	memcpy(&desc_rec, bptr, sizeof(desc_rec));
	LE32_CPU(desc_rec.d_id);
	LE32_CPU(desc_rec.desc_id);
	LE32_CPU(desc_rec.list_id);
	LE32_CPU(parent_id);
	bptr+=sizeof(desc_rec);
	continue;
      }
      // When duplicates found, just update the info.... perhaps this is correct functionality
      DEBUG_INDEX(("pst_load_index: Searching for existing record\n"));

      if (desc_rec.d_id <= *high_id && (d_ptr = _pst_getDptr(pf, desc_rec.d_id)) !=  NULL) { 
	DEBUG_INDEX(("pst_load_index: Updating Existing Values\n"));
	d_ptr->list_index = _pst_getID(pf, desc_rec.list_id);
	d_ptr->desc = _pst_getID(pf, desc_rec.desc_id);
	DEBUG_INDEX(("pst_load_index: \tdesc = %#x\tlist_index=%#x\n", 
		    (d_ptr->desc==NULL?0:d_ptr->desc->id), 
		    (d_ptr->list_index==NULL?0:d_ptr->list_index->id)));
	if (d_ptr->parent != NULL && desc_rec.parent_id != d_ptr->parent->id) {
	  DEBUG_INDEX(("pst_load_index: WARNING -- Parent of record has changed. Moving it\n"));
	  //hmmm, we must move the record.
	  // first we must remove from current location
	  //   change previous record to point next to our next
	  //     if no previous, then use parent's child
	  //     if no parent then change pf->d_head;
	  //   change next's prev to our prev
	  //     if no next then change parent's child_tail
	  //     if no parent then change pf->d_tail
	  if (d_ptr->prev != NULL)
	    d_ptr->prev->next = d_ptr->next;
	  else if (d_ptr->parent != NULL)
	    d_ptr->parent->child = d_ptr->next;
	  else
	    pf->d_head = d_ptr->next;
	  
	  if (d_ptr->next != NULL)
	    d_ptr->next->prev = d_ptr->prev;
	  else if (d_ptr->parent != NULL)
	    d_ptr->parent->child_tail = d_ptr->prev;
	  else
	    pf->d_tail = d_ptr->prev;
	  
	  d_ptr->prev = NULL;
	  d_ptr->next = NULL;
	  d_ptr->parent = NULL;
	  
	  // ok, now place in correct place
	  DEBUG_INDEX(("pst_load_index: Searching for parent\n"));
	  if (desc_rec.parent_id == 0) {
	    DEBUG_INDEX(("pst_load_index: No Parent\n"));
	    if (pf->d_tail != NULL)
	      pf->d_tail->next = d_ptr;
	    if (pf->d_head == NULL)
	      pf->d_head = d_ptr;
	    d_ptr->prev = pf->d_tail;
	    pf->d_tail = d_ptr;
	  } else {
	    d_ptr_ptr = d_ptr_head;
	    while (d_ptr_ptr != NULL && d_ptr_ptr->ptr->id != desc_rec.parent_id) {
	      d_ptr_ptr = d_ptr_ptr->next;
	    }

	    if (d_ptr_ptr == NULL && (d_par = _pst_getDptr(pf, desc_rec.parent_id)) == NULL) {
	      DEBUG_WARN(("pst_load_index: ERROR -- not found parent with id %#x\n", desc_rec.parent_id));
	      if (pf->d_tail != NULL)
		pf->d_tail->next = d_ptr;
	      if (pf->d_head == NULL)
		pf->d_head = d_ptr;
	      d_ptr->prev = pf->d_tail;
	      pf->d_tail = d_ptr;
	    } else {
	      if (d_ptr_ptr != NULL) 
		d_par = d_ptr_ptr->ptr;
	      else {
		//add the d_par to the cache
		DEBUG_INDEX(("pst_load_index: Update - Cache addition\n"));
		d_ptr_ptr = (struct _pst_d_ptr_ll*) xmalloc(sizeof(struct _pst_d_ptr_ll));
		d_ptr_ptr->prev = NULL;
		d_ptr_ptr->next = d_ptr_head;
		d_ptr_ptr->ptr = d_par;
		d_ptr_head = d_ptr_ptr;
		if (d_ptr_tail == NULL)
		  d_ptr_tail = d_ptr_ptr;
		d_ptr_count++;
		if (d_ptr_count > 100) {
		  //remove on from the end
		  d_ptr_ptr = d_ptr_tail;
		  d_ptr_tail = d_ptr_ptr->prev;
		  free (d_ptr_ptr);
		  d_ptr_count--;
		}
	      }
	      DEBUG_INDEX(("pst_load_index: Found a parent\n"));
	      d_par->no_child++;
	      d_ptr->parent = d_par;
	      if (d_par->child_tail != NULL)
		d_par->child_tail->next = d_ptr;
	      if (d_par->child == NULL)
		d_par->child = d_ptr;
	      d_ptr->prev = d_par->child_tail;
	      d_par->child_tail = d_ptr;
	    }
	  }
	}

      } else {     
	if (*high_id < desc_rec.d_id) {
	  DEBUG_INDEX(("pst_load_index: Updating New High\n"));
	  *high_id = desc_rec.d_id;
	}
	DEBUG_INDEX(("pst_load_index: New Record\n"));   
	d_ptr = (pst_desc_ll*) xmalloc(sizeof(pst_desc_ll));
	DEBUG_INDEX(("pst_load_index: Item pointer is %p\n", d_ptr));
	d_ptr->id = desc_rec.d_id;
	d_ptr->list_index = _pst_getID(pf, desc_rec.list_id);
	d_ptr->desc = _pst_getID(pf, desc_rec.desc_id);
	d_ptr->prev = NULL;
	d_ptr->next = NULL;
	d_ptr->parent = NULL;
	d_ptr->child = NULL;
	d_ptr->child_tail = NULL;
	d_ptr->no_child = 0;

        DEBUG_INDEX(("pst_load_index: Searching for parent\n"));
	if (desc_rec.parent_id == 0) {
	  DEBUG_INDEX(("pst_load_index: No Parent\n"));
	  if (pf->d_tail != NULL)
	    pf->d_tail->next = d_ptr;
	  if (pf->d_head == NULL)
	    pf->d_head = d_ptr;
	  d_ptr->prev = pf->d_tail;
	  pf->d_tail = d_ptr;
        } else {
	  d_ptr_ptr = d_ptr_head;
	  while (d_ptr_ptr != NULL && d_ptr_ptr->ptr->id != desc_rec.parent_id) {
	    d_ptr_ptr = d_ptr_ptr->next;
	  }
	  
	  if (d_ptr_ptr == NULL && (d_par = _pst_getDptr(pf, desc_rec.parent_id)) == NULL) {
	    DEBUG_WARN(("pst_load_index: ERROR -- not found parent with id %#x\n", desc_rec.parent_id));
	    if (pf->d_tail != NULL)
	      pf->d_tail->next = d_ptr;
	    if (pf->d_head == NULL)
	      pf->d_head = d_ptr;
	    d_ptr->prev = pf->d_tail;
	    pf->d_tail = d_ptr;
	  } else {
	    if (d_ptr_ptr != NULL) 
	      d_par = d_ptr_ptr->ptr;
	    else {
	      //add the d_par to the cache
	      DEBUG_INDEX(("pst_load_index: Normal - Cache addition\n"));
	      d_ptr_ptr = (struct _pst_d_ptr_ll*) xmalloc(sizeof(struct _pst_d_ptr_ll));
	      d_ptr_ptr->prev = NULL;
	      d_ptr_ptr->next = d_ptr_head;
	      d_ptr_ptr->ptr = d_par;
	      d_ptr_head = d_ptr_ptr;
	      if (d_ptr_tail == NULL)
		d_ptr_tail = d_ptr_ptr;
	      d_ptr_count++;
	      if (d_ptr_count > 100) {
		//remove one from the end
		d_ptr_ptr = d_ptr_tail;
		d_ptr_tail = d_ptr_ptr->prev;
		free (d_ptr_ptr);
		d_ptr_count--;
	      }
	    }
	    
	    DEBUG_INDEX(("pst_load_index: Found a parent\n"));
	    d_par->no_child++;
	    d_ptr->parent = d_par;
	    if (d_par->child_tail != NULL)
	      d_par->child_tail->next = d_ptr;
	    if (d_par->child == NULL)
	      d_par->child = d_ptr;
	    d_ptr->prev = d_par->child_tail;
	    d_par->child_tail = d_ptr;
	  }
	}
      }
      memcpy(&desc_rec, bptr, sizeof(desc_rec));
      LE32_CPU(desc_rec.d_id);
      LE32_CPU(desc_rec.desc_id);
      LE32_CPU(desc_rec.list_id);
      LE32_CPU(parent_id);
      bptr+= sizeof(desc_rec);
    }
    //    fseek(pf->fp, fpos, SEEK_SET);
  } else {
    // hopefully a table of offsets to more tables
    _pst_read_block_size(pf, offset, DESC_BLOCK_SIZE, &buf, 0, 0);
    bptr = buf;
    //    DEBUG_HEXDUMPC(buf, DESC_BLOCK_SIZE, 12);

    memcpy(&table, bptr, sizeof(table));
    LE32_CPU(table.start);
    LE32_CPU(table.u1);
    LE32_CPU(table.offset);
    bptr+=sizeof(table);
    memcpy(&table2, bptr, sizeof(table));
    LE32_CPU(table2.start);
    LE32_CPU(table2.u1);
    LE32_CPU(table2.offset);

    if (start_id != -1 && table.start != start_id) {
      DEBUG_WARN(("pst_load_index: This table isn't right. Perhaps we are too deep, or corruption\n"));
      if (buf) free (buf);
      return -1;
    }

    y = 0;
    while(table.start != 0 /*&& y < 0x1F && table.start < end_val*/) {
      DEBUG_INDEX(("pst_load_index: [%i] %i Pointer Table = [start = %#x, u1 = %#x, offset = %#x]\n", 
		  depth, ++y, table.start, table.u1, table.offset));
      

      if (table2.start < table.start) {
	// for the end of our table, table2.start may equal 0
	table2.start = end_val;
      }

      if ((i = _pst_build_desc_ptr(pf, table.offset, depth+1, high_id, table.start, table2.start)) == -1 && desc_depth_ok == 0) { //the table beneath isn't a table
	pf->index2_depth = depth; //set the max depth to this level
	if (buf) free(buf);
	return 4;
      } else if (i == 4) { //repeat with last tried values, but lower depth
	_pst_build_desc_ptr(pf, table.offset, depth+1, high_id, table.start, table2.start);
      }

      memcpy(&table, bptr, sizeof(table));
      LE32_CPU(table.start);
      LE32_CPU(table.u1);
      LE32_CPU(table.offset);
      bptr+=sizeof(table);
      memcpy(&table2, bptr, sizeof(table));
      LE32_CPU(table2.start);
      LE32_CPU(table2.u1);
      LE32_CPU(table2.offset);
    }
    if (buf) free(buf);
    return 3;
  }
  // ok, lets try freeing the d_ptr_head cache here
  while (d_ptr_head != NULL) {
    d_ptr_ptr = d_ptr_head->next;
    free(d_ptr_head);
    d_ptr_head = d_ptr_ptr;
  }
  if (buf) free(buf);
  return 0;
}

void* _pst_parse_item(pst_file *pf, pst_desc_ll *d_ptr) {
  pst_num_array * list;
  pst_index2_ll *id2_head = NULL;
  pst_index_ll *id_ptr = NULL;
  pst_item *item = NULL;
  pst_item_attach *attach = NULL;
  int x;

  if (d_ptr == NULL) {
    WARN(("_pst_parse_item: you cannot pass me a NULL! I don't want it!\n"));
    return NULL;
  }

  if (d_ptr->list_index != NULL) {
    id2_head = _pst_build_id2(pf, d_ptr->list_index);
  } else {
    DEBUG_WARN(("_pst_parse_item: Have not been able to fetch any id2 values for this item. Brace yourself!\n"));
  }

  if (d_ptr->desc == NULL) {
    DEBUG_WARN(("_pst_parse_item: why is d_ptr->desc == NULL? I don't want to do anything else with this record\n"));
    return NULL;
  }


  if ((list = _pst_parse_block(pf, d_ptr->desc, id2_head)) == NULL) {
    DEBUG_WARN(("_pst_parse_item: _pst_parse_block() returned an error for d_ptr->desc->id [%#x]\n", d_ptr->desc->id));
    return NULL;
  }

  item = (pst_item*) xmalloc(sizeof(pst_item));
  memset(item, 0, sizeof(pst_item));

  if (_pst_process(list, item)) {
    DEBUG_WARN(("_pst_parse_item: _pst_process() returned non-zero value. That is an error\n"));
    _pst_free_list(list);
    return NULL;
  } else {
    _pst_free_list(list);
    list = NULL; //_pst_process will free the items in the list
  }

  if ((id_ptr = _pst_getID2(id2_head, 0x671)) != NULL) {
    // attachements exist - so we will process them
    while (item->attach != NULL) {
      attach = item->attach->next;
      free(item->attach);
      item->attach = attach;
    }

    DEBUG_EMAIL(("_pst_parse_item: ATTACHEMENT processing attachement\n"));
    if ((list = _pst_parse_block(pf, id_ptr, id2_head)) == NULL) {
      DEBUG_WARN(("_pst_parse_item: ERROR error processing main attachment record\n"));
      return NULL;
    }
    x = 0;
    while (x < list->count_array) {
      attach = (pst_item_attach*) xmalloc (sizeof(pst_item_attach));
      memset (attach, 0, sizeof(pst_item_attach));
      attach->next = item->attach;
      item->attach = attach;
      x++;
    }
    item->current_attach = item->attach;

    if (_pst_process(list, item)) {
      DEBUG_WARN(("_pst_parse_item: ERROR _pst_process() failed with attachments\n"));
      _pst_free_list(list);
      return NULL;
    }
    _pst_free_list(list);

    // now we will have initial information of each attachment stored in item->attach...
    // we must now read the secondary record for each based on the id2 val associated with
    // each attachment
    attach = item->attach;
    while (attach != NULL) {
      if ((id_ptr = _pst_getID2(id2_head, attach->id2_val)) != NULL) {
	// id_ptr is a record describing the attachment
	if ((list = _pst_parse_block(pf, id_ptr, id2_head)) == NULL) {
	  DEBUG_WARN(("_pst_parse_item: ERROR error processing an attachment record\n"));
	  attach = attach->next;
	  continue;
	}
	item->current_attach = attach;
	if (_pst_process(list, item)) {
	  DEBUG_WARN(("_pst_parse_item: ERROR _pst_process() failed with an attachment\n"));
	  _pst_free_list(list);
	  attach = attach->next;
	  continue;
	}
	_pst_free_list(list);

      } else {
	DEBUG_WARN(("_pst_parse_item: ERROR cannot locate id2 value %#x\n", attach->id2_val));
      }
      attach = attach->next;
    }
    item->current_attach = item->attach; //reset back to first
  }

  _pst_free_id2(id2_head);

  if (item != NULL && item->email != NULL && item->email->subject != NULL &&
      item->email->subject->subj != NULL) {
    DEBUG_EMAIL(("item->email = %p\n", item->email));
    DEBUG_EMAIL(("item->email->subject = %p\n", item->email->subject));
    DEBUG_EMAIL(("item->email->subject->subj = %p\n", item->email->subject->subj));
  }

  return item;
}

pst_num_array * _pst_parse_block(pst_file *pf, pst_index_ll *block, pst_index2_ll *i2_head) {
  char *buf = NULL;
  pst_num_array *na_ptr = NULL, *na_head = NULL;
  pst_block_offset block_offset;
  pst_index_ll *rec = NULL;
  int size = 0, t_ptr = 0, fr_ptr = 0, to_ptr = 0, ind_ptr = 0, x = 0, stop = 0;
  int num_recs = 0, count_rec = 0, ind2_ptr = 0, list_start = 0, num_list = 0, cur_list = 0;
  int block_type, rec_size;
  
  struct {
    unsigned short int type;
    unsigned short int ref_type;
    unsigned int value;
  } table_rec; //for type 1 ("BC") blocks
  struct {
    unsigned short int ref_type;
    unsigned short int type;
    unsigned short int ind2_off;
    unsigned short int u1;
  } table2_rec; //for type 2 ("7C") blocks
  struct {
    unsigned short int index_offset;
    unsigned short int type;
    unsigned short int offset;
  } block_hdr;
  struct {
    unsigned char seven_c;
    unsigned char item_count;
    unsigned short int u1;
    unsigned short int u2;
    unsigned short int u3;
    unsigned short int rec_size;
    unsigned short int b_five_offset;
    unsigned short int u5;
    unsigned short int ind2_offset;
    unsigned short int u6;
    unsigned short int u7;
    unsigned short int u8;
  } seven_c_blk;
  struct _type_d_rec {
    unsigned int id;
    unsigned int u1;
  } * type_d_rec;

  if (block == NULL) {
    DEBUG_EMAIL(("_pst_parse_block(): block == NULL. Cannot continue with this block\n"));
    return NULL;
  }

  DEBUG_EMAIL(("_pst_parse_block(): About to read %i bytes from offset %#x\n", block->size, block->offset));

  if (_pst_read_block_size(pf, block->offset, block->size, &buf, PST_ENC, 0) < 0) {
    WARN(("_pst_parse_block(): Error reading block at offset %#x, size %#x\n", block->offset, block->size));
    if (buf) free (buf);
    return NULL;
  }
  DEBUG_EMAIL(("_pst_parse_block: pointer to buf is %p\n", buf));

  memcpy(&block_hdr, &(buf[0]), sizeof(block_hdr));
  LE16_CPU(block_hdr.index_offset);
  LE16_CPU(block_hdr.type);
  LE16_CPU(block_hdr.offset);

  ind_ptr = block_hdr.index_offset;
  
  if (block_hdr.type == 0xBCEC) { //type 1
    block_type = 1;
    
    _pst_getBlockOffset(buf, ind_ptr, block_hdr.offset, &block_offset);
    fr_ptr = block_offset.from;
    memcpy(&table_rec, &(buf[fr_ptr]), sizeof(table_rec));
    LE16_CPU(table_rec.type);
    LE16_CPU(table_rec.ref_type);
    LE32_CPU(table_rec.value);

    if (table_rec.type != 0x02B5) {
      WARN(("_pst_parse_block(): Unknown second block constant - %#X for id %#x [offset = %#x]\n", table_rec.type, block->id, block->offset));
      if (buf) free (buf);
      return NULL;
    }

    _pst_getBlockOffset(buf, ind_ptr, table_rec.value, &block_offset);
    list_start = fr_ptr = block_offset.from;
    to_ptr = block_offset.to;
    num_list = (to_ptr - fr_ptr)/sizeof(table_rec);
    num_recs = 1; // only going to one object in these blocks
    rec_size = 0; // doesn't matter cause there is only one object
  } else if (block_hdr.type == 0x7CEC) { //type 2
    block_type = 2;
    
    _pst_getBlockOffset(buf, ind_ptr, block_hdr.offset, &block_offset);
    fr_ptr = block_offset.from; //now got pointer to "7C block"
    memset(&seven_c_blk, 0, sizeof(seven_c_blk));
    memcpy(&seven_c_blk, &(buf[fr_ptr]), sizeof(seven_c_blk));
    LE16_CPU(seven_c_blk.u1);
    LE16_CPU(seven_c_blk.u2);
    LE16_CPU(seven_c_blk.u3);
    LE16_CPU(seven_c_blk.rec_size);
    LE16_CPU(seven_c_blk.b_five_offset);
    LE16_CPU(seven_c_blk.u5);
    LE16_CPU(seven_c_blk.ind2_offset);
    LE16_CPU(seven_c_blk.u6);
    LE16_CPU(seven_c_blk.u7);
    LE16_CPU(seven_c_blk.u8);

    list_start = fr_ptr + sizeof(seven_c_blk); // the list of item numbers start after this record

    if (seven_c_blk.seven_c != 0x7C) { // this would mean it isn't a 7C block!
      WARN(("_pst_parse_block(): Error. There isn't a 7C where I want to see 7C!\n"));
      if (buf) free(buf);
      return NULL;
    }

    rec_size = seven_c_blk.rec_size;
    num_list = seven_c_blk.item_count;
    DEBUG_EMAIL(("_pst_parse_block(): b5 offset = %#x\n", seven_c_blk.b_five_offset));

    _pst_getBlockOffset(buf, ind_ptr, seven_c_blk.b_five_offset, &block_offset);
    fr_ptr = block_offset.from;
    memcpy(&table_rec, &(buf[fr_ptr]), sizeof(table_rec));
    LE16_CPU(table_rec.type);
    LE16_CPU(table_rec.ref_type);
    LE32_CPU(table_rec.value);

    if (table_rec.type != 0x04B5) { // different constant than a type 1 record
      WARN(("_pst_parse_block(): Unknown second block constant - %#X for id %#x [offset = %#x]\n", table_rec.type, block->id, block->offset));
      if (buf) free(buf);
      return NULL;
    }

    if (table_rec.value == 0) { // this is for the 2nd index offset
      WARN(("_pst_parse_block(): reference to second index block is zero. ERROR\n"));
      if (buf) free(buf);
      return NULL;
    }

    _pst_getBlockOffset(buf, ind_ptr, table_rec.value, &block_offset);
    num_recs = (block_offset.to - block_offset.from) / 6; // this will give the number of records in this block
    
    _pst_getBlockOffset(buf, ind_ptr, seven_c_blk.ind2_offset, &block_offset);
    ind2_ptr = block_offset.from;
  } else {
    WARN(("_pst_parse_block(): ERROR: Unknown block constant - %#X for id %#x[offset = %#x]\n", block_hdr.type, block->id, block->offset));
    DEBUG_HEXDUMP(buf, block->size);
    if (buf) free(buf);
    return NULL;
  }

  DEBUG_EMAIL(("_pst_parse_block(): Mallocing number of items %i\n", num_recs));
  while (count_rec < num_recs) {
    na_ptr = (pst_num_array*) xmalloc(sizeof(pst_num_array));
    if (na_head == NULL) {
      na_head = na_ptr;
      na_ptr->next = NULL;
    }
    else {
      na_ptr->next = na_head;
      na_head = na_ptr;
    }
    // allocate an array of count num_recs to contain sizeof(struct_pst_num_item)
    na_ptr->items = (struct _pst_num_item**) xmalloc(sizeof(struct _pst_num_item)*num_list);
    na_ptr->count_item = num_list;
    na_ptr->count_array = num_recs; // each record will have a record of the total number of records
    x = 0;

    DEBUG_EMAIL(("_pst_parse_block(): going to read %i (%#x) items\n", na_ptr->count_item, na_ptr->count_item));

    fr_ptr = list_start; // init fr_ptr to the start of the list.
    cur_list = 0;
    stop = 0;
    while (!stop && cur_list < num_list) { //we will increase fr_ptr as we progress through index
      if (block_type == 1) {
	memcpy(&table_rec, &(buf[fr_ptr]), sizeof(table_rec));
	LE16_CPU(table_rec.type);
	LE16_CPU(table_rec.ref_type);
	fr_ptr += sizeof(table_rec);
      } else if (block_type == 2) {
	// we will copy the table2_rec values into a table_rec record so that we can keep the rest of the code
	memcpy(&table2_rec, &(buf[fr_ptr]), sizeof(table2_rec));
	LE16_CPU(table2_rec.ref_type);
	LE16_CPU(table2_rec.type);
	LE16_CPU(table2_rec.ind2_off);
	LE16_CPU(table2_rec.u1);

	// table_rec and table2_rec are arranged differently, so assign the values across
	table_rec.type = table2_rec.type;
	table_rec.ref_type = table2_rec.ref_type;

	memcpy(&(table_rec.value), &(buf[ind2_ptr+table2_rec.ind2_off]), sizeof(table_rec.value));

	fr_ptr += sizeof(table2_rec);
      } else {
	WARN(("_pst_parse_block(): Missing code for block_type %i\n", block_type));
	if (buf) free(buf);
	return NULL;
      }
      
      cur_list++; // get ready to read next bit from list
      DEBUG_EMAIL(("_pst_parse_block(): reading block %i (type=%#x, ref_type=%#x, value=%#x)\n",
		  x, table_rec.type, table_rec.ref_type, table_rec.value));
      
      na_ptr->items[x] = (struct _pst_num_item*) xmalloc(sizeof(struct _pst_num_item)); 
      //      DEBUG_EMAIL(("_pst_parse_block:   record address = %p\n", na_ptr->items[x]));
      memset(na_ptr->items[x], 0, sizeof(struct _pst_num_item)); //init it
      na_ptr->items[x]->id = table_rec.type; 
      
      /* Reference Types

         2 - 0x0002 - Signed 16bit value
	 3 - 0x0003 - Signed 32bit value
	11 - 0x000B - Boolean (non-zero = true)
	13 - 0x000D - Embedded Object
	30 - 0x001E - Null terminated String
	31 - 0x001F - Unicode string
	64 - 0x0040 - Systime - Filetime structure
	72 - 0x0048 - OLE Guid
       258 - 0x0102 - Binary data

	   - 0x1003 - Array of 32bit values
	   - 0x101E - Array of Strings
	   - 0x1102 - Array of Binary data
      */

      if (table_rec.ref_type == 0x0003 || table_rec.ref_type == 0x000b
	  || table_rec.ref_type == 0x0002) { //contains data 
	na_ptr->items[x]->data = xmalloc(sizeof(int)); 
	memcpy(na_ptr->items[x]->data, &(table_rec.value), sizeof(int)); 

	na_ptr->items[x]->size = sizeof(int);
	na_ptr->items[x]->type = table_rec.ref_type;

      } else if (table_rec.ref_type == 0x000D || table_rec.ref_type == 0x1003
		 || table_rec.ref_type == 0x001E || table_rec.ref_type == 0x0102
		 || table_rec.ref_type == 0x0040 || table_rec.ref_type == 0x101E
		 || table_rec.ref_type == 0x0048 || table_rec.ref_type == 0x1102) { 
	//contains index_ref to data 
	LE32_CPU(table_rec.value);
	if ((table_rec.value & 0x0000000F) > 0) { 
	  // if value ends in 'F' then this should be an id2 value 
	  DEBUG_EMAIL(("_pst_parse_block(): Found id2 [%#x] value. Will follow it\n", 
		      table_rec.value)); 
	  if ((rec = _pst_getID2(i2_head, table_rec.value)) != NULL) { 
	    na_ptr->items[x]->size = _pst_read_block_size(pf, rec->offset, rec->size, 
							  &(na_ptr->items[x]->data), PST_ENC, 
							  1); 
	  } else { 
	    DEBUG_EMAIL(("Record %#x not found\n", table_rec.value)); 
	    na_ptr->items[x]->size = 0;
	    na_ptr->items[x]->data = xmalloc(1);
	    na_ptr->items[x]->data[0] = '\0';
	  } 
	} else if (table_rec.value != 0) {
	  if ((table_rec.value >> 4)+ind_ptr > block->size) { 
	    // check that we will not be outside the buffer we have read
	    WARN(("_pst_parse_block(): table_rec.value [%#x] is outside of block\n",
		 table_rec.value));
	    na_ptr->count_item --;
	    continue;
	  }
	  if (_pst_getBlockOffset(buf, ind_ptr, table_rec.value, &block_offset)) { 
	    WARN(("_pst_parse_block(): failed to get block offset for table_rec.value of %#x\n", 
		 table_rec.value)); 
	    na_ptr->count_item --; //we will be skipping a row
	    continue; 
	  } 
	  t_ptr = block_offset.from; 
	  na_ptr->items[x]->size = size = block_offset.to - t_ptr; 
	  if (size < 0) {
	    WARN(("_pst_parse_block(): I don't want to malloc less than zero sized block. "
		 "Will change to 1 byte\n"));
	    na_ptr->items[x]->size = size = 0; // the malloc statement will add one to this
	  }

	  if (table_rec.ref_type == 0xd && size != 4) {
	    DEBUG_EMAIL(("_pst_parse_block(): TYPE 0xD IS NOT 4 BYTES BIG\n"));
	  }

	  // plus one for good luck (and strings) we will null terminate all reads
	  na_ptr->items[x]->data = (char*) xmalloc(size+1); 
	  memcpy(na_ptr->items[x]->data, &(buf[t_ptr]), size);
	  na_ptr->items[x]->data[size] = '\0'; // null terminate buffer

	  if (table_rec.ref_type == 0xd) {
	    // there is still more to do for the type of 0xD
	    type_d_rec = (struct _type_d_rec*) na_ptr->items[x]->data;
	    LE32_CPU(type_d_rec->id);
	    if ((rec = _pst_getID(pf, type_d_rec->id)) != NULL) {
	      na_ptr->items[x]->size = _pst_read_block_size(pf, rec->offset, rec->size,
							    &(na_ptr->items[x]->data), PST_ENC,
							    1);
	    } else {
	      DEBUG_EMAIL(("Record %#x not found\n", type_d_rec->id));
	      na_ptr->items[x]->size = 0;
	      na_ptr->items[x]->data = realloc(na_ptr->items[x]->data, 1);
	      na_ptr->items[x]->data[0] = '\0';
	    }
	  }
	} else {
	  DEBUG_EMAIL(("_pst_parse_block(): Ignoring 0 value in offset\n"));
	  if (na_ptr->items[x]->data)
	    free (na_ptr->items[x]->data);
	  free(na_ptr->items[x]);
	  
	  na_ptr->count_item--; // remove this item from the destination list
	  continue;
	}
	na_ptr->items[x]->type = table_rec.ref_type;
      } else {
	WARN(("_pst_parse_block(): ERROR Unknown ref_type %#x\n", table_rec.ref_type));
	return NULL;
      }
      x++;
    }
    DEBUG_EMAIL(("_pst_parse_block(): increasing ind2_ptr by %i [%#x] bytes. Was %#x, Now %#x\n",
		rec_size, rec_size, ind2_ptr, 
		ind2_ptr+rec_size));
    ind2_ptr += rec_size;
    count_rec++;
  }
  if (buf != NULL)
    free(buf);
  return na_head;
}

// check if item->email is NULL, and init if so
#define MALLOC_EMAIL(x) { if (x->email == NULL) { x->email = (pst_item_email*) xmalloc(sizeof(pst_item_email)); memset (x->email, 0, sizeof(pst_item_email));} }
#define MALLOC_FOLDER(x) { if (x->folder == NULL) { x->folder = (pst_item_folder*) xmalloc(sizeof(pst_item_folder)); memset (x->folder, 0, sizeof(pst_item_folder));} }
#define MALLOC_CONTACT(x) { if (x->contact == NULL) { x->contact = (pst_item_contact*) xmalloc(sizeof(pst_item_contact)); memset(x->contact, 0, sizeof(pst_item_contact));} }
#define MALLOC_MESSAGESTORE(x) { if (x->message_store == NULL) { x->message_store = (pst_item_message_store*) xmalloc(sizeof(pst_item_message_store)); memset(x->message_store, 0, sizeof(pst_item_message_store)); } }

// malloc space and copy the current item's data -- plus one on the size for good luck (and string termination)
#define LIST_COPY(targ, type) { \
  targ = type realloc(targ, list->items[x]->size+1); \
  memset(targ, 0, list->items[x]->size+1); \
  memcpy(targ, list->items[x]->data, list->items[x]->size); \
}

/*  free(list->items[x]->data); \
    list->items[x]->data=NULL; \*/

#define INC_CHECK_X() { if (++x >= list->count_item) break; }
#define NULL_CHECK(x) { if (x == NULL) { DEBUG_EMAIL(("NULL_CHECK: Null Found\n")); break;} }

#define MOVE_NEXT(targ) { \
  if (next){\
    if ((char*)targ == NULL) {\
      DEBUG_EMAIL(("MOVE_NEXT: Target is NULL. Will stop processing this option\n"));\
      break;\
    }\
    targ = targ->next;\
    if ((char*)targ == NULL) {\
      DEBUG_EMAIL(("MOVE_NEXT: Target is NULL after next. Will stop processing this option\n"));\
      break;\
    }\
    next=0;\
  }\
}
 
int _pst_process(pst_num_array *list , pst_item *item) {
  int x, t;
  int next = 0;
  pst_item_attach *attach;

  if (item == NULL) {
    DEBUG_EMAIL(("_pst_process(): item cannot be NULL.\n"));
    return -1;
  }

  attach = item->current_attach; // a working variable
  DEBUG_EMAIL(("_pst_process: Size of pst_item_email = %#x\n", sizeof(pst_item_email)));

  while (list != NULL) {
    x = 0;
    while (x < list->count_item) {
      if (list->items[x]->id == 0x0002) { // PR_ALTERNATE_RECIPIENT_ALLOWED
	// If set to true, the sender allows this email to be autoforwarded
	DEBUG_EMAIL(("_pst_process: AutoForward allowed - "));
	MALLOC_EMAIL(item);
	if (*((short int*)list->items[x]->data) != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->autoforward = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->autoforward = -1;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0017) { // PR_IMPORTANCE 
	// How important the sender deems it to be
	// 0 - Low
	// 1 - Normal
	// 2 - High

	DEBUG_EMAIL(("_pst_process: Importance Level - "));
	MALLOC_EMAIL(item);
        memcpy(&(item->email->importance), list->items[x]->data, sizeof(item->email->importance));
	LE32_CPU(item->email->importance);
	t = item->email->importance;
	DEBUG_EMAIL(("%s [%i]\n", (t==0?"Low":(t==1?"Normal":"High")), t));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x001A) { // PR_MESSAGE_CLASS Ascii type of messages - NOT FOLDERS
	DEBUG_EMAIL(("_pst_process: IPM.x - "));
	LIST_COPY(item->ascii_type, (char*));
	if (strncmp("IPM.Note", item->ascii_type, 8) == 0)
	  item->type = PST_TYPE_NOTE;
	else if (strncmp("IPM.Contact", item->ascii_type, 11) == 0)
	  item->type = PST_TYPE_CONTACT;
	else
	  item->type = PST_TYPE_OTHER;

	DEBUG_EMAIL(("%s\n", item->ascii_type));
	INC_CHECK_X(); //increment x here so that the next if statement has a chance of matching the next item
      }
      if (list->items[x]->id == 0x0023) { // PR_ORIGINATOR_DELIVERY_REPORT_REQUESTED
	// set if the sender wants a delivery report from all recipients
	DEBUG_EMAIL(("_pst_process: Global Delivery Report - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->delivery_report = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->delivery_report = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0026) { // PR_PRIORITY
	// Priority of a message
	// -1 NonUrgent
	//  0 Normal
	//  1 Urgent
	DEBUG_EMAIL(("_pst_process: Priority - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->priority), list->items[x]->data, sizeof(item->email->priority));
	LE32_CPU(item->email->priority);
	t = item->email->priority;
	DEBUG_EMAIL(("%s [%i]\n", (t<0?"NonUrgent":(t==0?"Normal":"Urgent")), t));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0029) { // PR_READ_RECEIPT_REQUESTED
	DEBUG_EMAIL(("_pst_process: Read Receipt - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->read_receipt = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->read_receipt = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x002E) { // PR_ORIGINAL_SENSITIVITY
	// the sensitivity of the message before being replied to or forwarded
	// 0 - None
	// 1 - Personal
	// 2 - Private
	// 3 - Company Confidential
	DEBUG_EMAIL(("_pst_process: Original Sensitivity - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->orig_sensitivity), list->items[x]->data, sizeof(item->email->orig_sensitivity));
	LE32_CPU(item->email->orig_sensitivity);
	t = item->email->orig_sensitivity;
	DEBUG_EMAIL(("%s [%i]\n", (t==0?"None":(t==1?"Personal":
						(t==2?"Private":"Company Confidential"))), t));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0036) { // PR_SENSITIVITY
	// sender's opinion of the sensitivity of an email
	// 0 - None
	// 1 - Personal
	// 2 - Private
	// 3 - Company Confidiential
	DEBUG_EMAIL(("_pst_process: Sensitivity - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->sensitivity), list->items[x]->data, sizeof(item->email->sensitivity));
	LE32_CPU(item->email->sensitivity);
	t = item->email->sensitivity;
	DEBUG_EMAIL(("%s [%i]\n", (t==0?"None":(t==1?"Personal":
						(t==2?"Private":"Company Confidential"))), t));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0037) { // PR_SUBJECT raw subject
	DEBUG_EMAIL(("_pst_process: Raw Subject - "));
	MALLOC_EMAIL(item);
	item->email->subject = (pst_item_email_subject*) realloc(item->email->subject, sizeof(pst_item_email_subject));
	memset(item->email->subject, 0, sizeof(pst_item_email_subject));
	DEBUG_EMAIL((" [size = %i] ", list->items[x]->size));
	if (list->items[x]->size > 0) {
	  if (isprint(list->items[x]->data[0])) {
	    // then there are no control bytes at the front
	    item->email->subject->off1 = 0;
	    item->email->subject->off2 = 0;
	    item->email->subject->subj = realloc(item->email->subject->subj, list->items[x]->size+1);
	    memset(item->email->subject->subj, 0, list->items[x]->size+1);
	    memcpy(item->email->subject->subj, list->items[x]->data, list->items[x]->size);
	  } else {
	    DEBUG_EMAIL(("\n_pst_process: Raw Subject has control codes\n"));
	    // there might be some control bytes in the first and second bytes
	    item->email->subject->off1 = list->items[x]->data[0];
	    item->email->subject->off2 = list->items[x]->data[1];
	    item->email->subject->subj = realloc(item->email->subject->subj, (list->items[x]->size-2)+1);
	    memset(item->email->subject->subj, 0, list->items[x]->size-1);
	    memcpy(item->email->subject->subj, &(list->items[x]->data[2]), list->items[x]->size-2);
	  }
	  DEBUG_EMAIL(("%s\n", item->email->subject->subj));
	  DEBUG_EMAIL(("item->email->subject->subj = %p\n", item->email->subject->subj));
	} else {
	  // obviously outlook has decided not to be straight with this one.
	  item->email->subject->off1 = 0;
	  item->email->subject->off2 = 0;
	  item->email->subject = NULL;
	  DEBUG_EMAIL(("NULL subject detected\n"));
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0039) { // PR_CLIENT_SUBMIT_TIME Date Email Sent/Created
	DEBUG_EMAIL(("_pst_process: Date sent - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sent_date, (FILETIME*));
#ifndef _MSC_VER
	DEBUG_EMAIL(("%s", FileTimeToAscii(item->email->sent_date)));
#else
	DEBUG_EMAIL(("Not Printed in Windows\n"));
#endif
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x003B) { // PR_SENT_REPRESENTING_SEARCH_KEY Sender address 1
	DEBUG_EMAIL(("_pst_process: Sent on behalf of address 1 - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->outlook_sender, (char*));
	DEBUG_EMAIL(("%s\n", item->email->outlook_sender));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x003F) { // PR_RECEIVED_BY_ENTRYID Structure containing Recipient
	DEBUG_EMAIL(("_pst_process: Recipient Structure 1 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0040) { // PR_RECEIVED_BY_NAME Name of Recipient Structure
	DEBUG_EMAIL(("_pst_process: Received By Name 1 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0041) { // PR_SENT_REPRESENTING_ENTRYID Structure containing Sender
	DEBUG_EMAIL(("_pst_process: Sent on behalf of Structure 1 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0042) { // PR_SENT_REPRESENTING_NAME Name of Sender Structure
	DEBUG_EMAIL(("_pst_process: Sent on behalf of Structure Name - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->outlook_sender_name, (char*));
	DEBUG_EMAIL(("%s\n", item->email->outlook_sender_name));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0043) { // PR_RCVD_REPRESENTING_ENTRYID Recipient Structure 2
	DEBUG_EMAIL(("_pst_process: Received on behalf of Structure -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0044) { // PR_RCVD_REPRESENTING_NAME Name of Recipient Structure 2
	DEBUG_EMAIL(("_pst_process: Received on behalf of Structure Name -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x004F) { // PR_REPLY_RECIPIENT_ENTRIES Reply-To Structure
	DEBUG_EMAIL(("_pst_process: Reply-To Structure -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0050) { // PR_REPLY_RECIPIENT_NAMES Name of Reply-To Structure
	DEBUG_EMAIL(("_pst_process: Name of Reply-To Structure -"));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->reply_to, (char*));
	DEBUG_EMAIL(("%s\n", item->email->reply_to));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0051) { // PR_RECEIVED_BY_SEARCH_KEY Recipient Address 1
	DEBUG_EMAIL(("_pst_process: Recipient's Address 1 (Search Key) - "));
	MALLOC_EMAIL(item);
	LIST_COPY (item->email->outlook_recipient, (char*));
	DEBUG_EMAIL(("%s\n", item->email->outlook_recipient));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0052) { // PR_RCVD_REPRESENTING_SEARCH_KEY Recipient Address 2
	DEBUG_EMAIL(("_pst_process: Received on behalf of Address (Search Key) - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->outlook_recipient2, (char*));
	DEBUG_EMAIL(("%s\n", item->email->outlook_recipient2));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0057) { // PR_MESSAGE_TO_ME
	// this user is listed explicitly in the TO address
	DEBUG_EMAIL(("_pst_process: My address in TO field - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->message_to_me = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->message_to_me = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0058) { // PR_MESSAGE_CC_ME
	// this user is listed explicitly in the CC address
	DEBUG_EMAIL(("_pst_process: My address in CC field - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->message_cc_me = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->message_cc_me = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0059) { //PR_MESSAGE_RECIP_ME
	// this user appears in TO, CC or BCC address list
	DEBUG_EMAIL(("_pst_process: Message addressed to me - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->message_recip_me = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->message_recip_me = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0064) { // PR_SENT_REPRESENTING_ADDRTYPE Access method for Sender Address
	DEBUG_EMAIL(("_pst_process: Sent on behalf of address type - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sender_access, (char*));
	DEBUG_EMAIL(("%s\n", item->email->sender_access));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0065) { // PR_SENT_REPRESENTING_EMAIL_ADDRESS Sender Address
	DEBUG_EMAIL(("_pst_process: Sent on behalf of Address - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sender_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->sender_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0070) { // PR_CONVERSATION_TOPIC Processed Subject
	DEBUG_EMAIL(("_pst_process: Processed Subject (Conversation Topic) - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->proc_subject, (char*));
	DEBUG_EMAIL(("%s\n", item->email->proc_subject));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0071) { // PR_CONVERSATION_INDEX Date 2
	DEBUG_EMAIL(("_pst_process: Conversation Index - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->conv_index), list->items[x]->data, sizeof(item->email->conv_index));
	DEBUG_EMAIL(("%i\n", item->email->conv_index));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0075) { // PR_RECEIVED_BY_ADDRTYPE Recipient Access Method
	DEBUG_EMAIL(("_pst_process: Received by Address type - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->recip_access, (char*));
	DEBUG_EMAIL(("%s\n", item->email->recip_access));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0076) { // PR_RECEIVED_BY_EMAIL_ADDRESS Recipient Address
	DEBUG_EMAIL(("_pst_process: Received by Address - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->recip_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->recip_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0077) { // PR_RCVD_REPRESENTING_ADDRTYPE Recipient Access Method 2
	DEBUG_EMAIL(("_pst_process: Received on behalf of Address type - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->recip2_access, (char*));
	DEBUG_EMAIL(("%s\n", item->email->recip2_access));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0078) { // PR_RCVD_REPRESENTING_EMAIL_ADDRESS Recipient Address 2
	DEBUG_EMAIL(("_pst_process: Received on behalf of Address -"));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->recip2_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->recip2_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x007D) { // PR_TRANSPORT_MESSAGE_HEADERS Internet Header
	DEBUG_EMAIL(("_pst_process: Internet Header - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->header, (char*));
	//DEBUG_EMAIL(("%s\n", item->email->header));
	DEBUG_EMAIL(("NOT PRINTED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0C19) { // PR_SENDER_ENTRYID Sender Structure 2
	DEBUG_EMAIL(("_pst_process: Sender Structure 2 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0C1A) { // PR_SENDER_NAME Name of Sender Structure 2
	DEBUG_EMAIL(("_pst_process: Name of Sender Structure 2 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0C1D) { // PR_SENDER_SEARCH_KEY Name of Sender Address 2
	DEBUG_EMAIL(("_pst_process: Name of Sender Address 2 (Sender search key) - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->outlook_sender2, (char*));
	DEBUG_EMAIL(("%s\n", item->email->outlook_sender2));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0C1E) { // PR_SENDER_ADDRTYPE Sender Address 2 access method
	DEBUG_EMAIL(("_pst_process: Sender Address type - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sender2_access, (char*));
	DEBUG_EMAIL(("%s\n", item->email->sender2_access));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0C1F) { // PR_SENDER_EMAIL_ADDRESS Sender Address 2
	DEBUG_EMAIL(("_pst_process: Sender Address - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sender2_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->sender2_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E01) { // PR_DELETE_AFTER_SUBMIT
	// I am not too sure how this works
	DEBUG_EMAIL(("_pst_process: Delete after submit - "));
	MALLOC_EMAIL(item);
	if (*(short int*) list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->delete_after_submit = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->delete_after_submit = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E03) { // PR_DISPLAY_CC CC Addresses
	DEBUG_EMAIL(("_pst_process: Display CC Addresses - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->cc_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->cc_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E04) { // PR_DISPLAY_TO Address Sent-To
	DEBUG_EMAIL(("_pst_process: Display Sent-To Address - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sentto_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->sentto_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E06) { // PR_MESSAGE_DELIVERY_TIME Date 3 - Email Arrival Date
	DEBUG_EMAIL(("_pst_process: Date 3 (Delivery Time) - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->arrival_date, (FILETIME*));
#ifndef _MSC_VER
	DEBUG_EMAIL(("%s", FileTimeToAscii(item->email->arrival_date)));
#else
	DEBUG_EMAIL(("Not Printed in Windows\n"));
#endif
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E07) { // PR_MESSAGE_FLAGS Email Flag
	// 0x01 - Read
	// 0x02 - Unmodified
	// 0x04 - Submit
	// 0x08 - Unsent
	// 0x10 - Has Attachments
	// 0x20 - From Me
	// 0x40 - Associated
	// 0x80 - Resend
	// 0x100 - RN Pending
	// 0x200 - NRN Pending
	DEBUG_EMAIL(("_pst_process: Message Flags - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->flag), list->items[x]->data, sizeof(item->email->flag));
	LE32_CPU(item->email->flag);
	DEBUG_EMAIL(("%i\n", item->email->flag));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E08) { // PR_MESSAGE_SIZE Total size of a message object
	DEBUG_EMAIL(("_pst_process: Message Size - "));
	memcpy(&(item->message_size), list->items[x]->data, sizeof(item->message_size));
	LE32_CPU(item->email->flag);
	DEBUG_EMAIL(("%i [%#x]\n", item->message_size, item->message_size));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E0A) { // PR_SENTMAIL_ENTRYID
	// folder that this message is sent to after submission
	DEBUG_EMAIL(("_pst_process: Sentmail EntryID - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->sentmail_folder, (pst_entryid*));
	LE32_CPU(item->email->sentmail_folder->id);
	DEBUG_EMAIL(("[id = %#x]\n", item->email->sentmail_folder->id));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E1F) { // PR_RTF_IN_SYNC
	// True means that the rtf version is same as text body
	// False means rtf version is more up-to-date than text body
	// if this value doesn't exist, text body is more up-to-date than rtf and
	//   cannot update to the rtf
	DEBUG_EMAIL(("_pst_process: Compressed RTF in Sync - "));
	MALLOC_EMAIL(item);
	if (*(short int*)list->items[x]->data != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->email->rtf_in_sync = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->email->rtf_in_sync = -1;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0E20) { // PR_ATTACH_SIZE binary Attachment data in record
	DEBUG_EMAIL(("_pst_process: Attachment Size - "));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	LIST_COPY(attach->data, (char*));
	attach->size = list->items[x]->size;
	DEBUG_EMAIL(("%i\n", attach->size));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x0FF9) { // PR_RECORD_KEY Record Header 1
	DEBUG_EMAIL(("_pst_process: Record Key 1 - "));
	LIST_COPY(item->record_key, (char*));
	item->record_key_size = list->items[x]->size;
	DEBUG_EMAIL_HEXPRINT(item->record_key, item->record_key_size);
	DEBUG_EMAIL(("\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1000) { // PR_BODY Plain Text body
	DEBUG_EMAIL(("_pst_process: Plain Text body - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->body, (char*));
	//DEBUG_EMAIL("%s\n", item->email->body);
	DEBUG_EMAIL(("NOT PRINTED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1006) { // PR_RTF_SYNC_BODY_CRC
	DEBUG_EMAIL(("_pst_process: RTF Sync Body CRC - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->rtf_body_crc), list->items[x]->data, 
	       sizeof(item->email->rtf_body_crc));
	LE32_CPU(item->email->rtf_body_crc);
	DEBUG_EMAIL(("%#x\n", item->email->rtf_body_crc));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1007) { // PR_RTF_SYNC_BODY_COUNT
	// a count of the *significant* charcters in the rtf body. Doesn't count
	// whitespace and other ignorable characters
	DEBUG_EMAIL(("_pst_process: RTF Sync Body character count - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->rtf_body_char_count), list->items[x]->data, 
	       sizeof(item->email->rtf_body_char_count));
	LE32_CPU(item->email->rtf_body_char_count);
	DEBUG_EMAIL(("%i [%#x]\n", item->email->rtf_body_char_count, 
		     item->email->rtf_body_char_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1008) { // PR_RTF_SYNC_BODY_TAG
	// the first couple of lines of RTF body so that after modification, then beginning can
	// once again be found
	DEBUG_EMAIL(("_pst_process: RTF Sync body tag - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->rtf_body_tag, (char*));
	DEBUG_EMAIL(("%s\n", item->email->rtf_body_tag));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1009) { // PR_RTF_COMPRESSED
	// some compression algorithm has been applied to this. At present
	// it is unknown
	DEBUG_EMAIL(("_pst_process: RTF Compressed body - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->rtf_compressed, (char*));
	DEBUG_EMAIL(("NOT PRINTED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1010) { // PR_RTF_SYNC_PREFIX_COUNT
	// a count of the ignored characters before the first significant character
	DEBUG_EMAIL(("_pst_process: RTF whitespace prefix count - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->rtf_ws_prefix_count), list->items[x]->data, 
	       sizeof(item->email->rtf_ws_prefix_count));
	DEBUG_EMAIL(("%i\n", item->email->rtf_ws_prefix_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1011) { // PR_RTF_SYNC_TRAILING_COUNT
	// a count of the ignored characters after the last significant character
	DEBUG_EMAIL(("_pst_process: RTF whitespace tailing count - "));
	MALLOC_EMAIL(item);
	memcpy(&(item->email->rtf_ws_trailing_count), list->items[x]->data,
	       sizeof(item->email->rtf_ws_trailing_count));
	DEBUG_EMAIL(("%i\n", item->email->rtf_ws_trailing_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1013) { // HTML body
	DEBUG_EMAIL(("_pst_process: HTML body - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->htmlbody, (char*));
	//DEBUG_EMAIL("%s\n", item->email->htmlbody);
	DEBUG_EMAIL(("NOT PRINTED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1035) { // Message ID
	DEBUG_EMAIL(("_pst_process: Message ID - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->messageid, (char*));
	DEBUG_EMAIL(("%s\n", item->email->messageid));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1042) { // in-reply-to
	DEBUG_EMAIL(("_pst_process: In-Reply-To - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->in_reply_to, (char*));
	DEBUG_EMAIL(("%s\n", item->email->in_reply_to));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x1046) { // Return Path
	DEBUG_EMAIL(("_pst_process: Return Path - "));
	MALLOC_EMAIL(item);
	LIST_COPY(item->email->return_path_address, (char*));
	DEBUG_EMAIL(("%s\n", item->email->return_path_address));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3001) { // PR_DISPLAY_NAME File As
	DEBUG_EMAIL(("_pst_process: Display Name - "));
	LIST_COPY(item->file_as, (char*));
	DEBUG_EMAIL(("%s\n", item->file_as));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3004) { // PR_COMMENT Comment for item - usually folders
	DEBUG_EMAIL(("_pst_process: Comment - "));
	LIST_COPY(item->comment, (char*));
	DEBUG_EMAIL(("%s\n", item->comment));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3007) { // PR_CREATION_TIME Date 4 - Creation Date?
	DEBUG_EMAIL(("_pst_process: Date 4 (Item Creation Date) - "));
	LIST_COPY(item->create_date, (FILETIME*));
#ifndef _MSC_VER
	DEBUG_EMAIL(("%s", FileTimeToAscii(item->create_date)));
#else
	DEBUG_EMAIL(("NOT PRINTED ON WINDOWS\n"));
#endif
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3008) { // PR_LAST_MODIFICATION_TIME Date 5 - Modify Date
	DEBUG_EMAIL(("_pst_process: Date 5 (Modify Date) - "));
	LIST_COPY(item->modify_date, (FILETIME*));
#ifndef _MSC_VER
	DEBUG_EMAIL(("%s", FileTimeToAscii(item->modify_date)));
#else
	DEBUG_EMAIL(("NOT PRINTED ON WINDOWS\n"));
#endif
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x300B) { // PR_SEARCH_KEY Record Header 2
	DEBUG_EMAIL(("_pst_process: Record Search 2 -- NOT HANDLED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x35DF) { // PR_VALID_FOLDER_MASK
	// States which folders are valid for this message store
	// FOLDER_IPM_SUBTREE_VALID 0x1
	// FOLDER_IPM_INBOX_VALID   0x2
	// FOLDER_IPM_OUTBOX_VALID  0x4
	// FOLDER_IPM_WASTEBOX_VALID 0x8
	// FOLDER_IPM_SENTMAIL_VALID 0x10
	// FOLDER_VIEWS_VALID        0x20
	// FOLDER_COMMON_VIEWS_VALID 0x40
	// FOLDER_FINDER_VALID       0x80
	DEBUG_EMAIL(("_pst_process: Valid Folder Mask - "));
	MALLOC_MESSAGESTORE(item);
	memcpy(&(item->message_store->valid_mask), list->items[x]->data, sizeof(int));
	LE32_CPU(item->message_store->valid_mask);
	DEBUG_EMAIL(("%i\n", item->message_store->valid_mask));
	INC_CHECK_X();
      }

      if (list->items[x]->id == 0x35E0) { // PR_IPM_SUBTREE_ENTRYID Top of Personal Folder Record
	DEBUG_EMAIL(("_pst_process: Top of Personal Folder Record - "));
	MALLOC_MESSAGESTORE(item);
	LIST_COPY(item->message_store->top_of_personal_folder, (pst_entryid*));
	LE32_CPU(item->message_store->top_of_personal_folder->id);
	DEBUG_EMAIL(("[id = %#x]\n", item->message_store->top_of_personal_folder->id));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x35E3) { // PR_IPM_WASTEBASKET_ENTRYID Deleted Items Folder Record
	DEBUG_EMAIL(("_pst_process: Deleted Items Folder record - "));
	MALLOC_MESSAGESTORE(item);
	LIST_COPY(item->message_store->deleted_items_folder, (pst_entryid*));
	LE32_CPU(item->message_store->deleted_items_folder->id);
	DEBUG_EMAIL(("[id = %#x]\n", item->message_store->deleted_items_folder->id));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x35E7) { // PR_FINDER_ENTRYID Search Root Record
	DEBUG_EMAIL(("_pst_process: Search Root record - "));
	MALLOC_MESSAGESTORE(item);
	LIST_COPY(item->message_store->search_root_folder, (pst_entryid*));
	LE32_CPU(item->message_store->search_root_folder->id);
	DEBUG_EMAIL(("[id = %#x]\n", item->message_store->search_root_folder->id));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3602) { // PR_CONTENT_COUNT Number of emails stored in a folder
	DEBUG_EMAIL(("_pst_process: Folder Email Count - "));
	MALLOC_FOLDER(item);
	memcpy(&(item->folder->email_count), list->items[x]->data, sizeof(item->folder->email_count));
	LE32_CPU(item->folder->email_count);
	DEBUG_EMAIL(("%i\n", item->folder->email_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3603) { // PR_CONTENT_UNREAD Number of unread emails
	DEBUG_EMAIL(("_pst_process: Unread Email Count - "));
	MALLOC_FOLDER(item);
	memcpy(&(item->folder->unseen_email_count), list->items[x]->data, sizeof(item->folder->unseen_email_count));
	LE32_CPU(item->folder->unseen_email_count);
	DEBUG_EMAIL(("%i\n", item->folder->unseen_email_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x360A) { // PR_SUBFOLDERS Has children
	DEBUG_EMAIL(("_pst_process: Subfolders? - "));
	MALLOC_FOLDER(item);
	if (*((int*)list->items[x]->data) != 0) {
	  DEBUG_EMAIL(("True\n"));
	  item->folder->subfolder = 1;
	} else {
	  DEBUG_EMAIL(("False\n"));
	  item->folder->subfolder = 0;
	}
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3613) { // PR_CONTAINER_CLASS IPF.x
	DEBUG_EMAIL(("_pst_process: IPF.x - "));
	LIST_COPY(item->ascii_type, (char*));
	if (strncmp("IPF.Note", item->ascii_type, 8) == 0)
	  item->type = PST_TYPE_NOTE;
	else if (strncmp("IPF.Contact", item->ascii_type, 11) == 0)
	  item->type = PST_TYPE_CONTACT;
	else if (strncmp("IPF.Journal", item->ascii_type, 11) == 0)
	  item->type = PST_TYPE_JOURNAL;
	else if (strncmp("IPF.Appointment", item->ascii_type, 15) == 0)
	  item->type = PST_TYPE_APPOINTMENT;
	else if (strncmp("IPF.StickyNote", item->ascii_type, 14) == 0)
	  item->type = PST_TYPE_STICKYNOTE;
	else if (strncmp("IPF.Task", item->ascii_type, 8) == 0)
	  item->type = PST_TYPE_TASK;
	else
	  item->type = PST_TYPE_OTHER;

	DEBUG_EMAIL(("%s [%i]\n", item->ascii_type, item->type));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3617) { // PR_ASSOC_CONTENT_COUNT
	// associated content are items that are attached to this folder
	// but are hidden from users
	DEBUG_EMAIL(("_pst_process: Associate Content count - "));
	MALLOC_FOLDER(item);
	memcpy(&(item->folder->assoc_count), list->items[x]->data, sizeof(item->folder->assoc_count));
	LE32_CPU(item->folder->assoc_count);
	DEBUG_EMAIL(("%i [%#x]\n", item->folder->assoc_count, item->folder->assoc_count));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3701) { // PR_ATTACH_DATA_OBJ binary data of attachment
	DEBUG_EMAIL(("_pst_process: Binary Data [Size %i] - ", 
		    list->items[x]->size));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	LIST_COPY(attach->data, (char*));
	attach->size = list->items[x]->size;
	DEBUG_EMAIL(("NOT PRINTED\n"));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3704) { // PR_ATTACH_FILENAME Attachment filename (8.3)
	DEBUG_EMAIL(("_pst_process: Attachment Filename - "));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	LIST_COPY(attach->filename1, (char*));
	DEBUG_EMAIL(("%s\n", attach->filename1));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3705) { // PR_ATTACH_METHOD
	// 0 - No Attachment
	// 1 - Attach by Value
	// 2 - Attach by reference
	// 3 - Attach by ref resolve
	// 4 - Attach by ref only
	// 5 - Embedded Message
	// 6 - OLE
	DEBUG_EMAIL(("_pst_process: Attachement method - "));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	memcpy(&(attach->method), list->items[x]->data, sizeof(attach->method));
	LE32_CPU(attach->method);
	t = attach->method;
	DEBUG_EMAIL(("%s [%i]\n", (t==0?"No Attachment":
				   (t==1?"Attach By Value":
				    (t==2?"Attach By Reference":
				     (t==3?"Attach by Ref. Resolve":
				      (t==4?"Attach by Ref. Only":
				       (t==5?"Embedded Message":"OLE")))))),t));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x370B) { // PR_RENDERING_POSITION
	// position in characters that the attachment appears in the plain text body
	DEBUG_EMAIL(("_pst_process: Attachment Position - "));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	memcpy(&(attach->position), list->items[x]->data, sizeof(attach->position));
	LE32_CPU(attach->position);
	DEBUG_EMAIL(("%i [%#x]\n", attach->position));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3707) { // PR_ATTACH_LONG_FILENAME Attachment filename (long?)
	DEBUG_EMAIL(("_pst_process: Attachment Filename long - "));
	NULL_CHECK(attach);
	MOVE_NEXT(attach);
	LIST_COPY(attach->filename2, (char*));
	DEBUG_EMAIL(("%s\n", attach->filename2));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x370E) { // PR_ATTACH_MIME_TAG Mime type of encoding
	DEBUG_EMAIL(("_pst_process: Attachment mime encoding - "));
	MOVE_NEXT(attach);
	LIST_COPY(attach->mimetype, (char*));
	DEBUG_EMAIL(("%s\n", attach->mimetype));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3710) { // PR_ATTACH_MIME_SEQUENCE
	// sequence number for mime parts. Includes body
	DEBUG_EMAIL(("_pst_process: Attachment Mime Sequence - "));
	MOVE_NEXT(attach);
	memcpy(&(attach->sequence), list->items[x]->data, sizeof(attach->sequence));
	LE32_CPU(attach->sequence);
	DEBUG_EMAIL(("%i\n", attach->sequence));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3a06) { // PR_GIVEN_NAME Contact's first name
	DEBUG_EMAIL(("_pst_process: Contacts First Name - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->first_name, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->first_name));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3a0a) { // PR_INITIALS Contact's Initials
	DEBUG_EMAIL(("_pst_process: Contacts Initials - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->initials, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->initials));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x3a11) { // PR_SURNAME Contact's Surname
	DEBUG_EMAIL(("_pst_process: Contacts Surname - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->surname, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->surname));
	INC_CHECK_X();
      }
      /*      if (list->items[x]->id == 0x661E) { // PR_Schedule_Folder_EntryID
	DEBUG_EMAIL(("_pst_process: Schedule Folder Entry ID - "));
	MALLOC_MESSAGESTORE(item);
	LIST_COPY(item->message_store->schedule_folder, (pst_entryid*));
	DEBUG_EMAIL(("[id = %#x]\n", item->message_store->schedule_folder->id));
	INC_CHECK_X();
	}*/
      if (list->items[x]->id == 0x67F2) { // ID2 value of the attachments proper record
	DEBUG_EMAIL(("_pst_process: Attachment ID2 value - "));
	MOVE_NEXT(attach);
	memcpy(&(attach->id2_val), list->items[x]->data, sizeof(attach->id2_val));
	LE32_CPU(attach->id2_val);
	DEBUG_EMAIL(("%#x\n", attach->id2_val));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x67FF) { // Extra Property Identifier
	DEBUG_EMAIL(("_pst_process: Password checksum [0x67FF] - "));
	MALLOC_MESSAGESTORE(item);
	memcpy(&(item->message_store->pwd_chksum), list->items[x]->data, 
	       sizeof(item->message_store->pwd_chksum));
	DEBUG_EMAIL(("%#x\n", item->message_store->pwd_chksum));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x80cf) { // Access method for contact
	DEBUG_EMAIL(("_pst_process: Contact access method - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->access_method, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->access_method));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x80d0) { // Address 1 for contact
	DEBUG_EMAIL(("_pst_process: Contact address 1 - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->address1, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->address1));
	INC_CHECK_X();
      }
      if (list->items[x]->id == 0x80d1) { // Address 1 description for contact 
	DEBUG_EMAIL(("_pst_process: Contact address 1 description - "));
	MALLOC_CONTACT(item);
	LIST_COPY(item->contact->address1_desc, (char*));
	DEBUG_EMAIL(("%s\n", item->contact->address1_desc));
	INC_CHECK_X();
      }
      if (x < list->count_item) {
      /* Reference Types

         2 - 0x0002 - Signed 16bit value
	 3 - 0x0003 - Signed 32bit value
	11 - 0x000B - Boolean (non-zero = true)
	13 - 0x000D - Embedded Object
	30 - 0x001E - Null terminated String
	31 - 0x001F - Unicode string
	64 - 0x0040 - Systime - Filetime structure
	72 - 0x0048 - OLE Guid
       258 - 0x0102 - Binary data

	   - 0x1003 - Array of 32bit values
	   - 0x101E - Array of Strings
	   - 0x1102 - Array of Binary data
      */
	DEBUG_EMAIL(("_pst_process: Unknown id [%#x, size=%#x]\n", list->items[x]->id, list->items[x]->size));
	if (list->items[x]->type == 0x02) {
	  DEBUG_EMAIL(("_pst_process: 16bit int = %hi\n", *(int*)list->items[x]->data));
	} else if (list->items[x]->type == 0x03) {
	  DEBUG_EMAIL(("_pst_process: 32bit int = %i\n", *(int*)list->items[x]->data));
	} else if (list->items[x]->type == 0x0b) {
	  DEBUG_EMAIL(("_pst_process: 16bit boolean = %s [%hi]\n", 
		       (*((short int*)list->items[x]->data)!=0?"True":"False"), 
		       *((short int*)list->items[x]->data)));
	} else if (list->items[x]->type == 0x1e) {
	  DEBUG_EMAIL(("_pst_process: String Data = \"%s\" [%#x]\n", 
		      list->items[x]->data, list->items[x]->type));
	} else if (list->items[x]->type == 0x40) {
#ifndef _MSC_VER
	  DEBUG_EMAIL(("_pst_process: Date = \"%s\" [%#x]\n",
		      FileTimeToAscii((FILETIME*)list->items[x]->data), 
		      list->items[x]->type));
#else    
	  DEBUG_EMAIL(("_pst_process: Not Printed on Windows\n"));
#endif
	} else if (list->items[x]->type == 0x102) {
	  DEBUG_EMAIL(("_pst_process: Binary Data [size = %#x]\n", 
		       list->items[x]->size));
	  DEBUG_HEXDUMP(list->items[x]->data, list->items[x]->size);
	} else if (list->items[x]->type == 0x101E) {
	  DEBUG_EMAIL(("_pst_process: Array of Strings [%#x]\n",
		      list->items[x]->type));
	} else {
	  DEBUG_EMAIL(("_pst_process: Not Printable [%#x]\n",
		      list->items[x]->type));
	}
	if (list->items[x]->data != NULL) {
	  free(list->items[x]->data);
	  list->items[x]->data = NULL;
	}
	INC_CHECK_X();
      }
    }
    x = 0;
    list = list->next;
    next = 1;
  }
  return 0;
}

int _pst_free_list(pst_num_array *list) {
  int x = 0;
  pst_num_array *l;

  while (list != NULL) {
    //    DEBUG_EMAIL(("_pst_free_list: About to free %i items\n", list->count_item));
    while (x < list->count_item) {
      if (list->items[x]->data != NULL) {
	//DEBUG_EMAIL(("+pst_free_list: Freeing data for item %i [%p]\n", x, list->items[x]->data));
	free (list->items[x]->data);
      }
      if (list->items[x] != NULL) {
	//	DEBUG_EMAIL(("_pst_free_list: Freeing item %i [%p]\n", x, list->items[x]));
	free (list->items[x]);
      }
      x++;
    }
    if (list->items != NULL) {
      //      DEBUG_EMAIL(("_pst_free_list: Freeing list->items\n"));
      free(list->items);
    }
    l = list;
    list = list->next;
    //    DEBUG_EMAIL(("_pst_free_list: Freeing list\n"));
    free (l);
    x = 0;
  }
  return 1;
}

int _pst_free_id2(pst_index2_ll * head) {
  pst_index2_ll *t;

  while (head != NULL) {
    t = head->next;
    free (head);
    head = t;
  }
  return 1;
}

int _pst_free_id (pst_index_ll *head) {
  pst_index_ll *t;
  while (head != NULL) {
    t = head->next;
    free(head);
    head = t;
  }
  return 1;
}

int _pst_free_desc (pst_desc_ll *head) {
  pst_desc_ll *t;
  while (head != NULL) {
    while (head->child != NULL) {
      head = head->child;
    }
    
    // point t to the next item
    t = head->next;
    if (t == NULL && head->parent != NULL) {
      t = head->parent;
      t->child = NULL; // set the child to NULL so we don't come back here again!
    }  

    if (head != NULL)
      free(head);
    else {
      DIE(("_pst_free_desc: head is NULL"));
    }

    head = t;
  }
  return 1;
}

pst_index2_ll * _pst_build_id2(pst_file *pf, pst_index_ll* list) {
  pst_block_header block_head;
  pst_index2_ll *head = NULL, *tail = NULL;
  int x = 0, b_ptr = 0;
  char *buf = NULL;
  pst_id2_assoc id2_rec;
  pst_index_ll *i_ptr = NULL;
  pst_index2_ll *i2_ptr = NULL;

  if (_pst_read_block_size(pf, list->offset, list->size, &buf, PST_NO_ENC,0) < 0) {
    //an error occured in block read
    WARN(("_pst_build_id2(): block read error occured. offset = %#x, size = %#x\n", list->offset, list->size));
    return NULL;
  }

  memcpy(&block_head, &(buf[0]), sizeof(block_head));
  LE16_CPU(block_head.type);
  LE16_CPU(block_head.count);

  if (block_head.type != 0x0002) { // some sort of constant?
    WARN(("_pst_build_id2(): Unknown constant [%#x] at start of id2 values [offset %#x].\n", block_head.type, list->offset));
    return NULL;
  }

  DEBUG_INDEX(("_pst_build_id2(): ID %#x is likely to be a description record. Count is %i (offset %#x)\n",
	      list->id, block_head.count, list->offset));
  x = 0;
  b_ptr = 0x04;
  while (x < block_head.count) {
    memcpy(&id2_rec, &(buf[b_ptr]), sizeof(id2_rec));
    LE32_CPU(id2_rec.id2);
    LE32_CPU(id2_rec.id);
    LE32_CPU(id2_rec.table2);

    b_ptr += sizeof(id2_rec);
    DEBUG_INDEX(("_pst_build_id2(): \tid2 = %#x, id = %#x, table2 = %#x\n", id2_rec.id2, id2_rec.id, id2_rec.table2));
    if ((i_ptr = _pst_getID(pf, id2_rec.id)) == NULL) {
      DEBUG_WARN(("_pst_build_id2(): \t\t%#x - Not Found\n", id2_rec.id));
    } else {
      DEBUG_INDEX(("_pst_build_id2(): \t\t%#x - Offset %#x, u1 %#x, Size %i(%#x)\n", i_ptr->id, i_ptr->offset, i_ptr->u1, i_ptr->size, i_ptr->size));
      i2_ptr = (pst_index2_ll*) xmalloc(sizeof(pst_index2_ll));
      i2_ptr->id2 = id2_rec.id2;
      i2_ptr->id = i_ptr;
      i2_ptr->next = NULL;
      if (head == NULL)
	head = i2_ptr;
      if (tail != NULL)
	tail->next = i2_ptr;
      tail = i2_ptr;
      if (id2_rec.table2 != 0) {
	if ((i_ptr = _pst_getID(pf, id2_rec.table2)) == NULL) {
	  DEBUG_WARN(("_pst_build_id2(): \tTable2 [%#x] not found\n", id2_rec.table2));
	} else {
	  DEBUG_INDEX(("_pst_build_id2(): \tGoing deeper for table2 [%#x]\n", id2_rec.table2));
	  if ((i2_ptr = _pst_build_id2(pf, i_ptr)) != NULL) {
	    DEBUG_INDEX(("_pst_build_id2(): \t\tAdding new list onto end of current\n"));
	    if (head == NULL)
	      head = i2_ptr;
	    if (tail != NULL)
	      tail->next = i2_ptr;
	    while (i2_ptr->next != NULL)
	      i2_ptr = i2_ptr->next;
	    tail = i2_ptr;
	  }
	}
      }
    }
    x++;
  }
  if (buf != NULL) {
    free (buf);
  }
  return head;
}

// This version of free does NULL check first
#define SAFE_FREE(x) {if (x != NULL) free(x);}

int _pst_freeItem(pst_item *item) {
  pst_item_attach *t;

  if (item != NULL) {
    if (item->email) {
      SAFE_FREE(item->email->arrival_date);
      SAFE_FREE(item->email->body);
      SAFE_FREE(item->email->cc_address);
      SAFE_FREE(item->email->header);
      SAFE_FREE(item->email->htmlbody);
      SAFE_FREE(item->email->in_reply_to);
      SAFE_FREE(item->email->messageid);
      SAFE_FREE(item->email->outlook_recipient);
      SAFE_FREE(item->email->outlook_recipient2);
      SAFE_FREE(item->email->outlook_sender);
      SAFE_FREE(item->email->outlook_sender_name);
      SAFE_FREE(item->email->outlook_sender2);
      SAFE_FREE(item->email->proc_subject);
      SAFE_FREE(item->email->recip_access);
      SAFE_FREE(item->email->recip_address);
      SAFE_FREE(item->email->recip2_access);
      SAFE_FREE(item->email->recip2_address);
      SAFE_FREE(item->email->reply_to);
      SAFE_FREE(item->email->rtf_body_tag);
      SAFE_FREE(item->email->rtf_compressed);
      SAFE_FREE(item->email->return_path_address);
      SAFE_FREE(item->email->sender_access);
      SAFE_FREE(item->email->sender_address);
      SAFE_FREE(item->email->sender2_access);
      SAFE_FREE(item->email->sender2_address);
      SAFE_FREE(item->email->sent_date);
      SAFE_FREE(item->email->sentmail_folder);
      SAFE_FREE(item->email->sentto_address);
      if (item->email->subject != NULL)
	SAFE_FREE(item->email->subject->subj);
      SAFE_FREE(item->email->subject);
      free(item->email);
    }
    if (item->folder) {
      free(item->folder);
    }
    if (item->message_store) {
      SAFE_FREE(item->message_store->deleted_items_folder);
      SAFE_FREE(item->message_store->search_root_folder);
      SAFE_FREE(item->message_store->top_of_personal_folder);
      free(item->message_store);
    }
    if (item->contact) {
      SAFE_FREE(item->contact->access_method);
      SAFE_FREE(item->contact->address1);
      SAFE_FREE(item->contact->address1_desc);
      SAFE_FREE(item->contact->address2);
      SAFE_FREE(item->contact->first_name);
      SAFE_FREE(item->contact->surname);
      SAFE_FREE(item->contact->initials);
      free(item->contact);
    }
    while (item->attach != NULL) {
      SAFE_FREE(item->attach->filename1);
      SAFE_FREE(item->attach->filename2);
      SAFE_FREE(item->attach->mimetype);
      SAFE_FREE(item->attach->data);
      t = item->attach->next;
      free(item->attach);
      item->attach = t;
    }
    SAFE_FREE(item->create_date);
    SAFE_FREE(item->modify_date);
    SAFE_FREE(item->ascii_type);
    SAFE_FREE(item->file_as);
    SAFE_FREE(item->comment);
    free(item);
  }
  return 0;
}  

int _pst_getBlockOffset(char *buf, int i_offset, int offset, pst_block_offset *p) {
  if (p == NULL || buf == NULL || offset == 0)
    return -1;
  memcpy(&(p->from), &(buf[(i_offset+2)+(offset>>4)]), sizeof(p->from));
  memcpy(&(p->to), &(buf[(i_offset+2)+(offset>>4)+sizeof(p->from)]), sizeof(p->to));
  LE32_CPU(p->from);
  LE32_CPU(p->to);
  return 0;
}

pst_index_ll * _pst_getID(pst_file* pf, int id) {
  static pst_index_ll *old_val = NULL; //this should make it quicker
  pst_index_ll *ptr = old_val;

  if (id == 0)
    return NULL;

  if (id & 0x1) { // if the number is odd
    DEBUG_INDEX(("_pst_getID: ODD_INDEX (not even) is this a pointer to a table?\n"));
  }
  
  id &= 0xFFFFFFFE; // remove least sig. bit. seems that it might work if I do this

  DEBUG_INDEX(("_pst_getID: Trying to find %#x\n", id));
  
  if (ptr == NULL) 
    ptr = pf->i_head;
  while (ptr->id != id) {
    ptr = ptr->next;
    if (ptr == old_val)
      break;
    if (ptr == NULL) {
      ptr = pf->i_head;
      if (ptr == old_val)
	break;
    }
  }
  if (ptr == old_val && (ptr != NULL && ptr->id != id)) {
    ptr = NULL;
    DEBUG_INDEX(("_pst_getID(): ERROR: Value not found\n"));
  } else {
    old_val = ptr;
    DEBUG_INDEX(("_pst_getID(): Found Value\n"));
  }
  return ptr;
}

pst_index_ll * _pst_getID2(pst_index2_ll * ptr, int id) {
  while (ptr != NULL && ptr->id2 != id) {
    ptr = ptr->next;
  }
  if (ptr != NULL)
    return ptr->id;
  return NULL;
}

pst_desc_ll * _pst_getDptr(pst_file *pf, int id) {
  pst_desc_ll *ptr = pf->d_head;
  while(ptr != NULL && ptr->id != id) {
    if (ptr->child != NULL) {
      ptr = ptr->child;
      continue;
    }
    while (ptr->next == NULL && ptr->parent != NULL) {
      ptr = ptr->parent;
    }
    ptr = ptr->next;
  }
  return ptr; // will be NULL or record we are looking for
}

int _pst_printDptr(pst_file *pf) {
  pst_desc_ll *ptr = pf->d_head;
  int depth = 0, x;
  while (ptr != NULL) {
    DEBUG_INDEX(("_pst_printDptr: "));
    for (x = 0; x < depth; x++){
      DEBUG_INDEX((" "));
    }
    DEBUG_INDEX(("%#x [%i] desc=%#x, list=%#x\n", ptr->id, ptr->no_child, 
	  (ptr->desc==NULL?0:ptr->desc->id), 
	  (ptr->list_index==NULL?0:ptr->list_index->id)));
    if (ptr->child != NULL) {
      depth++;
      ptr = ptr->child;
      continue;
    }
    while (ptr->next == NULL && ptr->parent != NULL) {
      depth--;
      ptr = ptr->parent;
    }
    ptr = ptr->next;
  }
  return 0;
}

int _pst_printIDptr(pst_file* pf) {
  pst_index_ll *ptr = pf->i_head;
  while (ptr != NULL) {
    DEBUG_INDEX(("_pst_printIDptr: %#x offset=%#x size=%#x\n", ptr->id, ptr->offset, ptr->size));
    ptr = ptr->next;
  }
  return 0;
}
      
int _pst_read_block(FILE *fp, int offset, void **buf) {
  short int size;
  int fpos;
  DEBUG_READ(("_pst_read_block: Reading block from %#x\n", offset));
  fpos = ftell(fp);
  fseek(fp, offset, SEEK_SET);
  fread(&size, sizeof(short int), 1, fp);
  fseek(fp, offset, SEEK_SET);
  DEBUG_READ(("_pst_read_block: Allocating %i bytes\n", size));
  if (*buf != NULL) {
    DEBUG_READ(("_pst_read_block: Freeing old memory\n"));
    free(*buf);
  }
  *buf = (void*)xmalloc(size);
  size = fread(*buf, 1, size, fp);
  fseek(fp, fpos, SEEK_SET);
  return size;
}

// when the first byte of the block being read is 01, then we can assume 
// that it is a list of further ids to read and we will follow those ids
// recursively calling this function until we have all the data
// we could do decryption of the encrypted PST files here
int _pst_read_block_size(pst_file *pf, int offset, int size, char ** buf, int do_enc, 
			 char is_index) {
  int fpos, x, z;
  short int count, y;
  char *buf2 = NULL, *buf3 = NULL;
  unsigned char fdepth;
  pst_index_ll *ptr = NULL;
  
  DEBUG_READ(("_pst_read_block_size: Reading block from %#x, %i bytes\n", offset, size));
  fpos = ftell(pf->fp);
  fseek(pf->fp, offset, SEEK_SET);
  if (*buf != NULL) {
    DEBUG_READ(("_pst_read_block_size: Freeing old memory\n"));
    free(*buf);
  }

  *buf = (void*) xmalloc(size+1); //plus one so that we can NULL terminate it later
  size = fread(*buf, 1, size, pf->fp);

  if (is_index) {
    DEBUG_READ(("_pst_read_block_size: ODD_BLOCK should be here\n"));
    DEBUG_READ(("\t: byte 0-1: %#x %#x\n", (*buf)[0], (*buf)[1]));
  }

  if ((*buf)[0] == 0x01 && is_index) { 
    //don't do this recursion if we should be at a leaf node
    memcpy(&count, &((*buf)[2]), sizeof(short int));
    memcpy(&fdepth, &((*buf)[1]), sizeof(fdepth));
    DEBUG_READ(("_pst_read_block_size: Seen indexes to blocks. Depth is %i\n", fdepth));
    // do fancy stuff! :)
    DEBUG_READ(("_pst_read_block_size: There are %i ids\n", count));
    // if first 2 blocks are 01 01 then index to blocks
    size = 0;
    y = 0;
    while (y < count) {
      memcpy(&x, &(*buf)[0x08+(y*4)], sizeof(int));
      if ((ptr = _pst_getID(pf, x)) == NULL) {
	WARN(("_pst_read_block_size: PANIC!!! oh damn! this is a bad situation! Cannot find ID\n"));
	buf3 = (char*) realloc(buf3, size+1);
	buf3[size] = '\0';
	*buf = buf3;
	fseek(pf->fp, fpos, SEEK_SET);
	return size;
      }
      if ((z = _pst_read_block_size(pf, ptr->offset, ptr->size, &buf2, do_enc, fdepth-1)) == -1) {
	DEBUG_READ(("_pst_read_block_size: Aaahhh!\n"));
	buf3 = (char*) realloc(buf3, size+1);
	buf3[size] = '\0';
	*buf = buf3;
	fseek(pf->fp, fpos, SEEK_SET);
	return size;
      }
      DEBUG_READ(("_pst_read_block_size: Melding newley retrieved block with bigger one. New size is %i\n", size+z));
      buf3 = (char*) realloc(buf3, size+z+1); //plus one so that we can null terminate it later
      DEBUG_READ(("_pst_read_block_size: Doing copy. Start pos is %i, length is %i\n", size, z);
      memcpy(&(buf3[size]), buf2, z));
      size += z;
      y++;
    }
    free(*buf);
    if (buf2 != NULL)
      free(buf2);
    if (buf3 == NULL) { 
      // this can happen if count == 0. We should create an empty buffer so we don't
      // confuse any clients
      buf3 = (char*) xmalloc(1);
    }
    *buf = buf3;
  } else if (do_enc && pf->encryption)
    _pst_decrypt(*buf, size, pf->encryption);

  (*buf)[size] = '\0'; //should be byte after last one read
  fseek(pf->fp, fpos, SEEK_SET);
  return size;
}

int _pst_decrypt(unsigned char *buf, int size, int type) {
  int x = 0;
  unsigned char y;

  if (buf == NULL)
    return -1;

  if (type == PST_COMP_ENCRYPT) {
    x = 0;
    while (x < size) {
      y = buf[x];
      DEBUG_DECRYPT(("_pst_decrypt: Transposing %#hhx to %#hhx [%#x]\n", buf[x], comp_enc[y], y));
      buf[x] = comp_enc[y]; // transpose from encrypt array
      x++;
    }
  } else {
    WARN(("_pst_decrypt: Unknown encryption: %i. Cannot decrypt\n", type));
    return -1;
  }

  return 0;
}

int _pst_getAtPos(FILE *fp, int pos, void* buf, unsigned int size) {
  if (fseek(fp, pos, SEEK_SET) == -1) {
    return 1;
  }
  
  if (fread(buf, 1, size, fp) < size) {
    return 2;
  }
  return 0;
}

int _pst_get (FILE *fp, void *buf, unsigned int size) {
  //  DEBUG_READ(("_pst_get: Reading block of %i [%#x] bytes\n", size, size));
  if (fread(buf, 1,  size, fp) < size) {
    return 1;
  }
  return 0;
}

void * xmalloc(size_t size) {
  void *mem = malloc(size);
  if (mem == NULL) {
    fprintf(stderr, "xMalloc: Out Of memory\n");
    exit(1);
  }
  return mem;
}

		      
