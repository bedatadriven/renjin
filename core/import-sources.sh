#!/bin/sh

# This script is a record of the process of importing R language sources
# from the C-R project. 
#
# The goal is to avoid modifications to the R language sources so that 
# we can keep in sync with C-R's development.

R_SRC_ROOT=/home/alex/dev/R-2-14-2

# Base package sources
BASE=$R_SRC_ROOT/src/library/base
cp -u $BASE/baseloader.R $BASE/DESCRIPTION $BASE/makebasedb.R src/library/base
cp -u $BASE/R/*.R src/library/base/R
cp -u $BASE/R/windows/*.R src/library/base/R   # the windows impl of sytem() is closer to java

# Import only the Common profile from C-R; Renjin will set up the 
# .Library paths internally based on the class path or on arguments to the context
# generator

mkdir -p src/library/profile
cp -u $R_SRC_ROOT/src/library/profile/Common.R src/library/profile/

for PKG in "tools" "datasets" "stats" "graphics" "utils" "grDevices" "splines"
do
	# currently, we are only copying the R sources and
	# data folder; we don't have the time for the 
	
	mkdir -p src/library/$PKG
	mkdir -p src/library/$PKG/R
	mkdir -p src/library/$PKG/data	
	
	cp -u $R_SRC_ROOT/src/library/$PKG/DESCRIPTION src/library/$PKG
	cp -u $R_SRC_ROOT/src/library/$PKG/NAMESPACE src/library/$PKG
	if [ -d $R_SRC_ROOT/src/library/$PKG/R ];
	then
		cp -u $R_SRC_ROOT/src/library/$PKG/R/* src/library/$PKG/R
	fi
	if [ -d $R_SRC_ROOT/src/library/$PKG/R/windows ];
	then
		cp -u $R_SRC_ROOT/src/library/$PKG/R/windows/* src/library/$PKG/R
	fi
done

# Shared files
for DIR in "encodings" "licenses" "make" "R"
do
	mkdir -p src/main/resources/org/renjin/share/$DIR
	cp -u -R $R_SRC_ROOT/share/$DIR/* src/main/resources/org/renjin/share/$DIR
done

mkdir -p src/main/resources/org/renjin/etc
cp -u $R_SRC_ROOT/etc/repositories src/main/resources/org/renjin/etc
