all: readpst getidblock

libpst.o: libpst.c libpst.h define.h
	gcc -c -Wall -Werror libpst.c -g -o libpst.o

libstrfunc.o: libstrfunc.c libstrfunc.h
	gcc -c -Wall -Werror libstrfunc.c -g -o libstrfunc.o

debug.o: debug.c
	gcc -c debug.c -Wall -Werror -g -o debug.o

readpst: readpst.c define.h libpst.o timeconv.o libstrfunc.o common.h debug.o
#	ccmalloc gcc -Wall -Werror readpst.c -g -o readpst libpst.o timeconv.o libstrfunc.o debug.o
#	gcc -Wall -Werror readpst.c -g -o readpst libpst.o timeconv.o libstrfunc.o -lefence
	gcc -Wall -Werror readpst.c -g -o readpst libpst.o timeconv.o libstrfunc.o debug.o

timeconv.o: timeconv.c timeconv.h common.h
	gcc -c -Wall -Werror timeconv.c -g -o timeconv.o

getidblock: getidblock.c define.h libpst.o common.h debug.o
	gcc -Wall -Werror getidblock.c -g -o getidblock libpst.o debug.o timeconv.o

clean: 
	rm -f core readpst libpst.o timeconv.o libstrfunc.o debug.o getidblock *~ 

rebuild: clean all

