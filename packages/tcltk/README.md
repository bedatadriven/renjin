
# Tcl/Tk Package

The Tcl/Tk package is part of the standard R distribution and required by a number of packages.

The sources here were copied from GNU R 3.2.2 and intended to provide only stubs that allow packages which
depend on tcltk to properly load. Any actual calls to tcltk functions with throw an error.

There is actually a JVM-based implementation of Tcl called [Jacl](http://tcljava.sourceforge.net/docs/website/index.html),
so if anyone is particularly motivated to provide a true implementation of the tcltk package that might be a starting 
point.