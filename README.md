Introduction
============

Renjin is a new interpreter for the [R Language for Statistical 
Computing](http://www.r-project.org), built on the Java Virtual Machine.

The primary goals of the project are to provide a modern interpreter
that serves as a drop-in replacement for GNU-R, but is easier to
integrate with other systems, offers better performance, and is
more extensible.

Downloads
=========

Artifacts from the latest successful build are available from the build server at

http://build.bedatadriven.com/job/renjin/lastSuccessfulBuild/


Building
========

Renjin's build is organized by Maven 3.x. Download and install
Maven from:

http://maven.apache.org/download.cgi 

In addition to the standard Java tools, Renjin relies on a GCC-based
build chain to compile C/Fortran math routines to JVM byte code. 
These tools are in the early stages of development and are a bit
sensitive to different versions of GCC and to OS. 

If your platform is not listed below, you may need to experiment a 
bit or ask for help on the mailing list (renjin-dev@googlegroups.com).

Ubuntu 12+
----------

You can install GCC and friends through the APT package manager:

    sudo apt-get install build-essential gcc-4.6-plugin-dev gfortran

If you are using a 64-bit version of Ubuntu, you will need to
install additional libraries in order to have GCC cross compile
to 32-bits (Renjin uses JVM arrays to back pointers, and the JVM
limits array indices to 32-bits on all platforms)

    sudo apt-get install gcc-multilib
  
Fedora 17
---------

Fedora 17 comes with GCC 4.7 which is *NOT* yet working with Renjin.

    sudo yum install gcc gcc-plugin-devel gcc-gfortran
  
If you're running 64-bit Fedora, you will need the i686 libraries 
for cross compiling:

    sudo yum install  glibc.i686 glibc-devel.i686

    
Windows/Cygwin
--------------

I've tried quite a bit to get the build chain running on Cygwin/Windows,
but even after getting a [newer version of GCC running](http://cygwin.wikia.com/wiki/How_to_install_a_newer_version_of_GCC), I was unable 
to get GCC to compile and load our GCC plugin.

If anyone is able to get this working, share on the mailing list (see above), otherwise
we'll have to wait until our tool can bootstrap a pure-java version of GCC :-)
