#!/bin/sh
apt-get update
apt-get install make gcc-4.7 gcc-4.7-plugin-dev gfortran-4.7 g++-4.7 gcc-4.7.multilib g++-4.7-multilib unzip libz-dev -y
cp -r renjin /usr/local/lib/
gcc-4.7 -shared -xc++ -I `gcc-4.7 -print-file-name=plugin`/include -fPIC -fno-rtti -O2 plugin.c -lstdc++ -shared-libgcc -o /usr/local/lib/renjin/bridge.so
rm -r renjin plugin.c
