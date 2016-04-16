
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
1. Oracle JDK 1.6+
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

Note that this requires that your host machine has a *case-sensitive* 
filesystem. For Mac OS X, see instructions below on creating a case-sensitive volume
for Renjin development.

### Ubuntu 12+

Be sure to install the Oracle JDK; the version of the OpenJDK that ships
with Ubuntu is missing the javax.tools.JavaCompiler required
by Renjin's code generator.

You can install GCC and friends through the APT package manager:

    sudo apt-get install maven gcc-4.6 gcc-4.6-plugin-dev gfortran-4.6 g++-4.6

If you are using a 64-bit version of Ubuntu, you will need to
install additional libraries in order to have GCC cross compile
to 32-bits (Renjin uses JVM arrays to back pointers, and the JVM
limits array indices to 32-bits on all platforms)

    sudo apt-get install gcc-4.6.multilib

Then build:

    mvn clean install

From the root of the project.


### Mac OS X 10.10 (Yosemite)

Renjin must be built on a volume that is case-sensitive. You can
[check if your harddrive is case sensitive](http://apple.stackexchange.com/questions/71357/how-to-check-if-my-hd-is-case-sensitive-or-not#71360)
and, if needed, [create a small case-sensitive volume](https://coderwall.com/p/mgi8ja/case-sensitive-git-in-mac-os-x-like-a-pro)
just for Renjin.

This setup uses [Homebrew](http://brew.sh/), so if you do not have it yet, please install it.

Download and install JDK 8 from the [Oracle Website](http://www.oracle.com/technetwork/java/javase/downloads/index.html). 

You need to install the legacy GCC version 4.6 using Homebrew. We need an old formula for this:

    wget https://raw.githubusercontent.com/alexpennace/homebrew-versions/6f55c92c25f08bbc103196ac29e89de97ab36bdf/gcc46.rb -O /usr/local/Library/Formula/gcc46.rb

You should now be able to install GCC using

    brew install gcc46 --enable-fortran

Finally, we need to link the file libiberty.h

    ln /usr/local/Cellar/gcc46/4.6.4/lib/gcc/x86_64-apple-darwin14.0.0/4.6.4/plugin/include/libiberty-4.6.h /usr/local/Cellar/gcc46/4.6.4/lib/gcc/x86_64-apple-darwin14.0.0/4.6.4/plugin/include/libiberty.h

Install gettext from Homebrew by typing

    brew install gettext

Since OSX also comes with a (shady) version of gettext, we need to set the include path:

	export C_INCLUDE_PATH=`find /usr/local/Cellar/gettext -type d -name 'include' | head -n 1`

Finally, try

    mvn install


### Windows/Cygwin

I've tried quite a bit to get the build chain running on Cygwin/Windows,
but even after getting a [newer version of GCC running](http://cygwin.wikia.com/wiki/How_to_install_a_newer_version_of_GCC), I was unable
to get GCC to compile and load our GCC plugin.

If anyone is able to get this working, share on the mailing list (see above), otherwise
we'll have to wait until our tool can bootstrap a pure-java version of GCC :-)

### Other platforms

For other platforms, you may need to experiment a
bit or ask for help on the mailing list (renjin-dev@googlegroups.com).



