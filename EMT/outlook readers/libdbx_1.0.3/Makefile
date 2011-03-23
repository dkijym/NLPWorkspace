OBJECT_FILES=libdbx.o timeconv.o
HEADER_FILES=libdbx.h define.h common.h

all: readdbx readoe

readdbx: $(OBJECT_FILES) $(HEADER_FILES) readdbx.c
	gcc -I/usr/local/include -g -o readdbx readdbx.c $(OBJECT_FILES) 

readoe: $(OBJECT_FILES) $(HEADER_FILES) readoe.c
	gcc -I/usr/local/include -g -o readoe readoe.c $(OBJECT_FILES)

clean:
	rm -f $(OBJECT_FILES) readoe readdbx core *~

dist: clean
	cd ..; tar -zcvf libdbx.tgz libdbx/*

%.o: %.c $(HEADER_FILES)
	gcc -g -c $*.c $(DEBUG_FLAGS)
