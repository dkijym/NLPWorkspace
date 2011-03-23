// LibPST - Library for Accessing Outlook .pst files
// Dave Smith - davesmith@users.sourceforge.net

#ifndef LIBPST_H
#define LIBPST_H

#ifndef  _MSC_VER
// -- al_neid -- //
#ifndef FILETIME_DEFINED
#define FILETIME_DEFINED
// -- al_neid -- //Win32 Filetime struct - copied from WINE
typedef struct {
  unsigned int dwLowDateTime;
  unsigned int dwHighDateTime;
} FILETIME;
#endif //ifndef FILETIME_DEFINED
// -- al_neid -- //


// -- al_neid -- //


#endif //ifndef  _MSC_VER
#ifdef _MSC_VER
#include "windows.h"
// -- al_neid -- //
#endif // _MSC_VER


#define PST_VERSION "0.3.4"

#define PST_TYPE_NOTE 1
#define PST_TYPE_APPOINTMENT 8
#define PST_TYPE_CONTACT 9
#define PST_TYPE_JOURNAL 10
#define PST_TYPE_STICKYNOTE 11
#define PST_TYPE_TASK 12
#define PST_TYPE_OTHER 13

// defines whether decryption is done on this bit of data
#define PST_NO_ENC 0
#define PST_ENC 1

// defines types of possible encryption
#define PST_NO_ENCRYPT 0
#define PST_COMP_ENCRYPT 1
#define PST_ENCRYPT 2

typedef struct _pst_misc_6_struct {
  int i1;
  int i2;
  int i3;
  int i4;
  int i5;
  int i6;
} pst_misc_6;

typedef struct _pst_entryid_struct {
  char entryid[20];
  int id;
} pst_entryid;

typedef struct _pst_desc_struct {
  int d_id;
  int desc_id;
  int list_id;
  int parent_id;
} pst_desc;

typedef struct _pst_index_struct{
  int id;
  int offset;
  unsigned short int size;
  short int u1;
} pst_index;

typedef struct _pst_index_tree {
  int id;
  int offset;
  int size;
  int u1;
  struct _pst_index_tree * next;
} pst_index_ll;

typedef struct _pst_index2_tree {
  int id2;
  pst_index_ll *id;
  struct _pst_index2_tree * next;
} pst_index2_ll;

typedef struct _pst_desc_tree {
  int id;
  pst_index_ll * list_index;
  pst_index_ll * desc;
  int no_child;
  struct _pst_desc_tree * prev;
  struct _pst_desc_tree * next;
  struct _pst_desc_tree * parent;
  struct _pst_desc_tree * child;
  struct _pst_desc_tree * child_tail;
} pst_desc_ll;


/*typedef struct _pst_item_struct {
  char * ascii_type;
  char * ascii_type2;
  char * body;
  char * cc_address;
  FILETIME *date1;
  FILETIME *date2;
  FILETIME *date3;
  FILETIME *date4;
  FILETIME *date5;
  pst_misc_6 * deleted_items_folder;
  int    email_count;
  char * folder_name;
  char * header;
  char * htmlbody;
  char * in_reply_to;
  int    is_folder;
  char * messageid;
  char * outlook_recipient;
  char * outlook_recipient2;
  char * outlook_sender;
  char * outlook_sender_name;
  char * outlook_sender2;
  char * proc_subject;
  char * recip_access;
  char * recip_address;
  char * recip2_access;
  char * recip2_address;
  char * reply_to;
  char * return_path_address;
  char   isseen;
  pst_misc_6 * search_root_folder;
  char * sender_access;
  char * sender_address;
  char * sender2_access;
  char * sender2_address;
  char * sentto_address;
  char * subject;
  pst_misc_6 * top_of_personal_folder;
  int    type;
  int    unseen_email_count;
} pst_item;
*/

typedef struct _pst_item_email_subject {
  int off1;
  int off2;
  char *subj;
} pst_item_email_subject;

typedef struct _pst_item_email {
  FILETIME *arrival_date;
  int autoforward; // 1 = true, 0 = not set, -1 = false
  char *body;
  char *cc_address;
  int conv_index;
  int  delete_after_submit; // 1 = true, 0 = false
  int  delivery_report; // 1 = true, 0 = false
  int  flag;
  char *header;
  char *htmlbody;
  int  importance;
  char *in_reply_to;
  int  message_cc_me; // 1 = true, 0 = false
  int  message_recip_me; // 1 = true, 0 = false
  int  message_to_me; // 1 = true, 0 = false
  char *messageid;
  int  orig_sensitivity;
  char *outlook_recipient;
  char *outlook_recipient2;
  char *outlook_sender;
  char *outlook_sender_name;
  char *outlook_sender2;
  int  priority;
  char *proc_subject;
  int  read_receipt;
  char *recip_access;
  char *recip_address;
  char *recip2_access;
  char *recip2_address;
  char *reply_to;
  char *return_path_address;
  int  rtf_body_char_count;
  int  rtf_body_crc;
  char *rtf_body_tag;
  char *rtf_compressed;
  int  rtf_in_sync; // 1 = true, 0 = doesn't exist, -1 = false
  int  rtf_ws_prefix_count;
  int  rtf_ws_trailing_count;
  char *sender_access;
  char *sender_address;
  char *sender2_access;
  char *sender2_address;
  int  sensitivity;
  FILETIME *sent_date;
  pst_entryid *sentmail_folder;
  char *sentto_address;
  pst_item_email_subject *subject;
} pst_item_email;

typedef struct _pst_item_folder {
  int  email_count;
  int  unseen_email_count;
  int  assoc_count;
  char subfolder;
} pst_item_folder;
  
typedef struct _pst_item_message_store {
  pst_entryid *deleted_items_folder;
  pst_entryid *search_root_folder;
  pst_entryid *top_of_personal_folder;
  int valid_mask; // what folders the message store contains
  int pwd_chksum;
} pst_item_message_store;
  
typedef struct _pst_item_contact {
  char *access_method;
  char *address1;
  char *address1_desc;
  char *address2;
  char *first_name;
  char *surname;
  char *initials;
} pst_item_contact;

typedef struct _pst_item_attach {
  char *filename1;
  char *filename2;
  char *mimetype;
  char *data;
  int  size;
  int  id2_val;
  int  method;
  int  position;
  int  sequence;
  struct _pst_item_attach *next;
} pst_item_attach;

typedef struct _pst_item {
  struct _pst_item_email *email; // data reffering to email
  struct _pst_item_folder *folder; // data reffering to folder
  struct _pst_item_contact *contact; // data reffering to contact
  struct _pst_item_attach *attach; // linked list of attachments
  struct _pst_item_attach *current_attach; // pointer to current attachment
  struct _pst_item_message_store * message_store; // data referring to the message store
  int type;
  char *ascii_type;
  char *file_as;
  char *comment;
  char *record_key; // probably 16 bytes long.
  int record_key_size;
  int message_size;
  FILETIME *create_date;
  FILETIME *modify_date;
} pst_item;

typedef struct _pst_file {
  pst_index_ll *i_head, *i_tail;
  pst_index2_ll *i2_head;
  pst_desc_ll *d_head, *d_tail;
  int index1;
  int index1_count;
  int index2;
  int index2_count;
  FILE * fp;
  int size;
  unsigned char index1_depth;
  unsigned char index2_depth;
  unsigned char encryption;
} pst_file;

typedef struct _pst_block_offset {
  short int from;
  short int to;
} pst_block_offset;

struct _pst_num_item {
  int id;
  char *data;
  int type;
  unsigned int size;
};

typedef struct _pst_num_array {
  int count_item;
  int count_array;
  struct _pst_num_item ** items;
  struct _pst_num_array *next;
} pst_num_array;


// prototypes
// -- al_neid -- //
int pst_open(pst_file *pf, char *name, char *mode);
// -- al_neid -- //
int pst_close(pst_file *pf);
int pst_load_index (pst_file *pf);
int _pst_build_id_ptr(pst_file *pf, int offset, int depth, int start_val, int end_val);
int _pst_build_desc_ptr (pst_file *pf, int offset, int depth, int *high_id, 
			 int start_id, int end_val);
pst_item* _pst_getItem(pst_file *pf, pst_desc_ll *d_ptr);
void * _pst_parse_item (pst_file *pf, pst_desc_ll *d_ptr);
pst_num_array * _pst_parse_block(pst_file *pf, pst_index_ll *block, pst_index2_ll *i2_head);
int _pst_process(pst_num_array *list, pst_item *item);
int _pst_free_list(pst_num_array *list);
int _pst_freeItem(pst_item *item);
int _pst_free_id2(pst_index2_ll * head);
int _pst_free_id (pst_index_ll *head);
int _pst_free_desc (pst_desc_ll *head);
int _pst_getBlockOffset(char *buf, int i_offset, int offset, pst_block_offset *p);
pst_index2_ll * _pst_build_id2(pst_file *pf, pst_index_ll* list);
pst_index_ll * _pst_getID(pst_file* pf, int id);
pst_index_ll * _pst_getID2(pst_index2_ll * ptr, int id);
pst_desc_ll * _pst_getDptr(pst_file *pf, int id);
int _pst_read_block_size(pst_file *pf, int offset, int size, char ** buf, int do_enc,
			 char is_index);
int _pst_decrypt(unsigned char *buf, int size, int type);
int _pst_getAtPos(FILE *fp, int pos, void* buf, unsigned int size);
int _pst_get (FILE *fp, void *buf, unsigned int size);

// DEBUG functions 
int _pst_printDptr(pst_file *pf);
int _pst_printIDptr(pst_file* pf);
void * xmalloc(size_t size);

#endif
