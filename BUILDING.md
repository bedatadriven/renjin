
Compiling from Source
=====================

Introduction
------------

In addition to the standard Java tools, Renjin relies on a GCC-based
build chain to compile C/Fortran math routines to JVM byte code.
These tools are in the early stages of development and are a bit
sensitive to different versions of GCC. The current version of Renjin
requires GCC 4.6.x.

If your platform is not listed below, you may need to experiment a
bit or ask for help on the mailing list (renjin-dev@googlegroups.com).

Requirements
------------
1. Oracle JDK 1.6 or 1.7
2. Apache Maven 3+
3. GCC 4.6.x

### Ubuntu 12+

Be sure to install the Oracle JDK; the version of the OpenJDK that ships
with Ubuntu is missing the javax.tools.JavaCompiler required
by Renjin's code generator.

You can install GCC and friends through the APT package manager:

    sudo apt-get install maven gcc-4.6 gcc-4.6-plugin-dev gfortran-4.6

If you are using a 64-bit version of Ubuntu, you will need to
install additional libraries in order to have GCC cross compile
to 32-bits (Renjin uses JVM arrays to back pointers, and the JVM
limits array indices to 32-bits on all platforms)

    sudo apt-get install gcc-4.6.multilib


### Fedora 17

Fedora 17 comes with GCC 4.7 which is *NOT* yet working with Renjin.

You will need to build GCC 4.6 from sources first:

    # Install GCC's dependencies
    sudo yum install gcc gmp-devel mpfr-devel libmpc-devel make glibc-devel.i686

    # Download sources
    wget ftp://ftp.nluug.nl/mirror/languages/gcc/releases/gcc-4.6.4/gcc-core-4.6.4.tar.bz2
    wget ftp://ftp.nluug.nl/mirror/languages/gcc/releases/gcc-4.6.4/gcc-fortran-4.6.4.tar.bz2
    tar -xjf gcc-core-4.6.4.tar.bz2
    tar -xjf gcc-fortran-4.6.4.tar.bz2
    cd gcc-4.6.4
    ./configure --prefix=/opt/gcc-4.6.4 -- && make

    sudo yum install gcc gcc-plugin-devel gcc-gfortran

If you're running 64-bit Fedora, you will need the i686 libraries
for cross compiling:

    sudo yum install glibc.i686 glibc-devel.i686

### Windows/Cygwin

I've tried quite a bit to get the build chain running on Cygwin/Windows,
but even after getting a [newer version of GCC running](http://cygwin.wikia.com/wiki/How_to_install_a_newer_version_of_GCC), I was unable
to get GCC to compile and load our GCC plugin.

If anyone is able to get this working, share on the mailing list (see above), otherwise
we'll have to wait until our tool can bootstrap a pure-java version of GCC :-)

Building
--------

Start the build by executing

    mvn clean install

From the root of the project.
