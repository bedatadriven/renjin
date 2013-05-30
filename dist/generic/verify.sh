#!/bin/sh

FINAL_NAME=$1
ZIP_FILE=$FINAL_NAME-renjin-standalone.zip

echo Verifying $ZIP_FILE

if [ -d target/verify ]
then
    rm -rf target/verify
fi

mkdir -p target/verify
cd target/verify

unzip ../$ZIP_FILE

cd $FINAL_NAME

./bin/renjin -f ../../../smokeTest.R