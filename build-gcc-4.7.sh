#!/bin/sh
set -e

mkdir gcc-4.7
cd gcc-4.7
wget http://ftp.gnu.org/gnu/gcc/gcc-4.7.4/gcc-4.7.4.tar.gz
tar -xzf gcc-4.7.4.tar.gz
cd gcc-4.7.4

# Download required libraries
./contrib/download_prerequisites

# Various patches to ensure that GCC-4.7 compiles with newer versions of GCC

# https://gcc.gnu.org/legacy-ml/gcc-patches/2015-08/msg00375.html
patch -p1 <<EOP
diff --git a/gcc/cp/cfns.gperf b/gcc/cp/cfns.gperf
index 68acd3d..953262f 100644
--- a/gcc/cp/cfns.gperf
+++ b/gcc/cp/cfns.gperf
@@ -22,6 +22,9 @@ __inline
 static unsigned int hash (const char *, unsigned int);
 #ifdef __GNUC__
 __inline
+#ifdef __GNUC_STDC_INLINE__
+__attribute__ ((__gnu_inline__))
+#endif
 #endif
 const char * libc_name_p (const char *, unsigned int);
 %}
diff --git a/gcc/cp/cfns.h b/gcc/cp/cfns.h
index 1c6665d..6d00c0e 100644
--- a/gcc/cp/cfns.h
+++ b/gcc/cp/cfns.h
@@ -53,6 +53,9 @@ __inline
 static unsigned int hash (const char *, unsigned int);
 #ifdef __GNUC__
 __inline
+#ifdef __GNUC_STDC_INLINE__
+__attribute__ ((__gnu_inline__))
+#endif
 #endif
 const char * libc_name_p (const char *, unsigned int);
 /* maximum key range = 391, duplicates = 0 */
--
2.4.4
EOP

# https://gcc.gnu.org/git/gitweb.cgi?p=gcc.git;h=883312dc79806f513275b72502231c751c14ff72
sed -i 's/struct ucontext/ucontext_t/g' libgcc/config/i386/linux-unwind.h


# Now build...
cd ..
mkdir build
cd build
../gcc-4.7.4/configure --prefix=/usr/local  --program-suffix=-4.7  --enable-shared --enable-plugin --enable-languages=c,c++,fortran
make -j 4

# Done!
cd ../..
