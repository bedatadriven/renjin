#!/bin/sh

FINAL_NAME=$1
ZIP_FILE=distributions/renjin.zip

echo Verifying $ZIP_FILE

if [ -d build/verify ]
then
    rm -rf build/verify
fi

mkdir -p build/verify
cd build/verify

unzip ../$ZIP_FILE

cd $FINAL_NAME

./bin/renjin -f ../../../smokeTest.R