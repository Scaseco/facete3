.PHONY: all

all: download

target:
	mkdir target

download: target
	(cd target && wget -c http://downloads.linkedgeodata.org/experimental/germany-residential-buildings-20180719.virtuoso.db.bz2 && bzip2 -dk germany-residential-buildings-20180719.virtuoso.db.bz2)
	
