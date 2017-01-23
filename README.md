[![Build Status](http://build.renjin.org/buildStatus/icon?job=renjin)](http://build.renjin.org) 

Introduction
============

Renjin is a new interpreter for the [R Language for Statistical 
Computing](http://www.r-project.org), built on the Java Virtual Machine.

The primary goals of the project are to provide a modern interpreter
that serves as a drop-in replacement for GNU-R, but is easier to
integrate with other systems, offers better performance, and is
more extensible.

For more information, please visit http://www.renjin.org.

Maven Artifacts
===============

You can add Renjin to your Maven/Ivy/etc build by adding the following
dependency and repository:

```.xml
<dependencies>
  <dependency>
    <groupId>org.renjin</groupId>
    <artifactId>renjin-script-engine</artifactId>
    <version>RELEASE</version>
  </dependency>
</dependencies>
<repositories>
  <repository>
    <id>bedatadriven</id>
    <name>bedatadriven public repo</name>
    <url>https://nexus.bedatadriven.com/content/groups/public/</url>
  </repository>
</repositories>
```

Downloads
=========

Standalone distributions of Renjin are available for ad-hoc analysis. Downloads
are available at [Renjin.org](http://www.renjin.org).

Artifacts from the latest successful build are available from the build server
at https://nexus.bedatadriven.com/content/groups/public/org/renjin/.

Compiling from Source
=====================

At this time, building Renjin completely is only supported on Ubuntu 14.04.
The resulting Java builds are fully platform-independent.

See [BUILDING](BUILDING.md) for more information.

License
=======

Renjin is available under the GPLv2 (or higher) license, see [LICENSE](LICENSE.txt) for the
full text of the license. Renjin also includes code from R which is
redistributed here. R uses
[many different licenses](http://www.r-project.org/Licenses/) so check the file
sources to see which license applies.

Demonstration
=============

Try the sandboxed version: http://renjindemo.appspot.com/
