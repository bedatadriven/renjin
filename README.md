Introduction
============

Renjin is a new interpreter for the [R Language for Statistical 
Computing](http://www.r-project.org), built on the Java Virtual Machine.

The primary goals of the project are to provide a modern interpreter
that serves as a drop-in replacement for GNU-R, but is easier to
integrate with other systems, offers better performance, and is
more extensible.

Maven Artifacts
===============

You can add Renjin to your Maven/Ivy/etc build by adding the following
dependency and repository:

```
<dependencies>
  <dependency>
    <groupId>org.renjin</groupId>
    <artifactId>renjin-script-engine</artifactId>
    <version>0.7.0-RC2</version>
  </dependency>
</dependencies>
<repositories>
  <repository>
    <id>bedatadriven</id>
    <name>bedatadriven public repo</name>
    <url>http://nexus.bedatadriven.com/content/groups/public/</url>
  </repository>
</repositories>
```

Downloads
=========

Standalone distributions of Renjin are available for ad-hoc analysis:

[renjin-debian-package-0.7.0-RC2.deb](http://nexus.bedatadriven.com/content/groups/public/org/renjin/renjin-debian-package/0.7.0-RC2/renjin-debian-package-0.7.0-RC2.deb)
[renjin-generic-package-0.7.0-RC2.zip](http://nexus.bedatadriven.com/content/groups/public/org/renjin/renjin-generic-package/0.7.0-RC2/renjin-generic-package-0.7.0-RC2.zip)

Artifacts from the latest successful build are available from the build server at

http://build.bedatadriven.com/job/renjin/lastSuccessfulBuild/

Compiling from Source
=====================

At this time, building Renjin completely is only supported on a Linux system
with gcc-4.6 installed. The resulting java builds are fully platform-independent.

See [BUILDING](BUILDING.md) for more information.


License
=======

Renjin is available under the GPLv3 license, see LICENSE.txt for the full text
of the license. Renjin also includes code from R which is redistributed here.
R uses [many different licenses](http://www.r-project.org/Licenses/) so check
the file sources to see which license applies.
