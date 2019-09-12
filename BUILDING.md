
Compiling from Source
=====================

Introduction
------------

In addition to the standard Java tools, Renjin relies on a GCC-based
build chain to compile C/Fortran math routines to JVM byte code.
These tools are under active development and are
sensitive to different versions of GCC. The current version of Renjin
requires GCC 4.7.x.


Requirements
------------
1. JDK 1.8 Recommended
2. GCC 4.7

### Vagrant

Vagrant is a tool that helps manage virtual development environments and
will help you quickly setup a Virtual Box with all the tools needed
for Renjin's C/Fortran compile step.

Install Vagrant from https://www.vagrantup.com and then run the following
from the root of the Renjin git repository that calls the
[Vagrantfile](Vagrantfile):

    vagrant up
    vagrant ssh -c "cd /home/ubuntu/renjin && ./gradlew build"

Vagrant configures a shared directory on the VirtualBox guest machine
that includes the Renjin repository, so once the initial build
is complete you can work normally from your IDE on your own (host) machine.

Note that this requires that your host machine has a *case-sensitive* 
filesystem. For Mac OS X, you can
[check if your harddrive is case sensitive](http://apple.stackexchange.com/questions/71357/how-to-check-if-my-hd-is-case-sensitive-or-not#71360)
and, if needed, [create a small case-sensitive volume](https://coderwall.com/p/mgi8ja/case-sensitive-git-in-mac-os-x-like-a-pro)
just for Renjin.

Once you have run the build through Vagrant, then you should be able to
make iterative changes to the Java sources and debug via your IDE 
as normal.

### Ubuntu 16.04+

You can install the required tools through the APT package manager. 
A 64-bit architecture is required.

    sudo apt-get install openjdk-8-jdk make gcc-4.7 gcc-4.7-plugin-dev gfortran-4.7 g++-4.7 gcc-4.7.multilib g++-4.7-multilib

Then build:

    mvn clean install

From the root of the project.

### Other platforms

For other platforms, consider using Vagrant to bootstrap your build,
or you may need to experiment a bit. 

You can also ask for help on the mailing list (renjin-dev@googlegroups.com).



