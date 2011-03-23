default: 
	cd metdemo;make;cd ..

install: jar

jar: default
	jar cvfm emt.jar Manifest.txt metdemo/*.class metdemo/Attach/*.class metdemo/AlertTools/*.class metdemo/CliqueTools/*.class metdemo/DataBase/*.class metdemo/MachineLearning/*.class metdemo/Tables/*.class metdemo/Tools/*.class metdemo/Window/*.class metdemo/Parser/*.class metdemo/images/*.gif

clean:
	cd metdemo;make clean;cd ..; rm -f emt.jar
