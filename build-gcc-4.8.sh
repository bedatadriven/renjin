#!/bin/sh

mkdir gcc-4.8
cd gcc-4.8
wget http://ftp.gnu.org/gnu/gcc/gcc-4.8.5/gcc-4.8.5.tar.gz
tar -xzf gcc-4.8.5.tar.gz
cd gcc-4.8.5

# Download required libraries
./contrib/download_prerequisites

# Various patches to ensure that GCC-4.8 compiles with newer versions of GCC
sed -i 's/struct ucontext/ucontext_t/g' libgcc/config/i386/linux-unwind.h
sed -i 's/__res_state/struct __res_state/g' libsanitizer/tsan/tsan_platform_linux.cc
sed -i -e 's/__attribute__/\/\/__attribute__/g' gcc/cp/cfns.h

patch -p1 <<EOP
diff --git a/libsanitizer/asan/asan_linux.cc b/libsanitizer/asan/asan_linux.cc
index c504168..59087b9 100644
--- a/libsanitizer/asan/asan_linux.cc
+++ b/libsanitizer/asan/asan_linux.cc
@@ -29,6 +29,7 @@
 #include <dlfcn.h>
 #include <fcntl.h>
 #include <pthread.h>
+#include <signal.h>
 #include <stdio.h>
 #include <unistd.h>
 #include <unwind.h>
EOP

# Now build...
cd ..
mkdir build
cd build
../gcc-4.8.5/configure --prefix=/usr/local  --program-suffix=-4.8  --enable-shared --enable-plugin --enable-languages=c,c++,fortran
make -j 4
cd ../..
