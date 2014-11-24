
Compiling from Source
=====================

Introduction
------------

In addition to the standard Java tools, Renjin relies on a GCC-based
build chain to compile C/Fortran math routines to JVM byte code.
These tools are in the early stages of development and are a bit
sensitive to different versions of GCC. The current version of Renjin
requires GCC 4.6.x.


Requirements
------------
1. Oracle JDK 1.6 or 1.7
2. Apache Maven 3+
3. GCC 4.6.x

### Vagrant

Vagrant is tool that helps manage virtual development environments and
will help you quickly setup a Virtual Box with all the tools needed
for Renjin's C/Fortran compile step.

Install Vagrant from https://www.vagrantup.com and then run the following
from the root of the renjin git repository:

    vagrant up
    vagrant ssh -c "cd renjin && mvn clean install"

Vagrant configures a shared directory on the VirtualBox guest machine
that includes the renjin repository, so once the initial build
is complete you can work normally from your IDE on your own (host) machine.

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

Then build:

    mvn clean install

From the root of the project.

### Windows/Cygwin

I've tried quite a bit to get the build chain running on Cygwin/Windows,
but even after getting a [newer version of GCC running](http://cygwin.wikia.com/wiki/How_to_install_a_newer_version_of_GCC), I was unable
to get GCC to compile and load our GCC plugin.

If anyone is able to get this working, share on the mailing list (see above), otherwise
we'll have to wait until our tool can bootstrap a pure-java version of GCC :-)

### Other platforms

For other platforms, you may need to experiment a
bit or ask for help on the mailing list (renjin-dev@googlegroups.com).

