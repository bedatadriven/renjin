
# R Packages and GCC-Bridge

This document provides an overview of how and where the compilation
of R packages with native code is done. It highlights problem areas
that require additional development and points contributors to
starting points in the code.

The whole process is managed by the [NativePackageBuilder](../../packager/src/main/java/org/renjin/packaging/NativeSourceBuilder.java)
class.

Both the Maven Plugin and the command-line build tool delegate to this class.

## Building Locally

The Renjin CI system generates Maven pom.xml files for each project in
CRAN and BioConductor. There is a "Rebuild Locally" link on each page
that will copy a shell script to your clipboard then you can paste into the shell, 
for example:

    curl http://packages.renjin.org/package/org.renjin.cran/dplyr/0.5.0/build/189/rebuild.sh | sh

Building packages with native code is _only_ supported on x86_64 Ubuntu 16+,
so you may want to start a VM. The resulting JAR is of course platform independent.

Once the sources and the pom.xml file are downloaded, update the pom.xml 
file to use Renjin version 0.9.0-SNAPSHOT so that it will build against
your local build of Renjin.


## Compilation by GCC

R packages can provide their own `configure` and build scripts, just
override certain parts of the compiliation using a `Makevars` file, or
some combination of the two.

For this reason, NativePackageBuilder does not use the `org.renjin.gcc.Gcc` driver class
to compile the sources, but rather runs `make` using an adapted 
[shlib.mk](../../gnur-installation/src/main/resources/share/make/shlib.mk).

This makefile in turn relies on variables defined in an adapted [Makeconf](../../gnur-installation/src/main/resources/etc/Makeconf),
which also includes flags to enable Renjin's GCC plugin which dumps 
the compiled Gimple to JSON.

These adapted makefile scripts, as well as adapted R headers, are included
in the [gnur-installation](../../gnur-installation) module and
are unpacked in the `target/` directory by `NativePackageBuilder`.

These variables can be overriden by package-provided `Makevars` files.
If a `src/Makevars.renjin` file is present, it will be used instead.

The end result is that sources are compiled with standard GNU R flags,
package-provided flags, _and_ Renjin's flags to support GCC-Bridge:

    g++-4.7 -I/home/alex/dev/cran/dplyr_0.5.0-b189/target/gnur/include \
        -DNDEBUG -I../inst/include -DCOMPILING_DPLYR  \
        -I"/home/alex/dev/cran/dplyr_0.5.0-b189/target/include"  \
        -DBOOST_NO_INT64_T -DBOOST_NO_INTEGRAL_INT64_T -DBOOST_NO_LONG_LONG \
        -fpic  -m32 \
        -fplugin=/home/alex/dev/cran/dplyr_0.5.0-b189/target/bridge.so \
        -DRENJIN \
        -g  \
        -fstack-protector --param=ssp-buffer-size=4 -Wformat -Werror=format-security -D_FORTIFY_SOURCE=2 \
        -c address.cpp -o address.o

A further complication, which is not yet fully addressed, is that configure
and build scripts can also shell out to R itself during the build process,
usually to query some details about the environment. A very simple [shell
script](../../gnur-installation/src/main/resources/bin/R) is currently
included in the `gnur-installation` module that does just enough to 
get a few packages compiled that we needed. A more general solution will 
need to be implemented at some point.

The [gcc-bridge GCC plugin](../gcc-bridge/gcc-plugin/src/main/c/plugin.c),
included by the `-fplugin=/path/to/bridge.so` flag,
dump the Gimple to a JSON file in the same directory, which is
read by the next stage.

## Parsing the Gimple

[Gimple](https://www.cse.iitb.ac.in/grc/gcc-workshop-09/downloads/gccw09-gimple.pdf)
is a *highly* simplified [intermediate representation](https://en.wikipedia.org/wiki/Intermediate_representation)
of the input source. 

The json files output by the gcc-bridge plugin are parsed by the
GimpleParser class, which largely relies on the Jackson library and annotations
to build an object model from the JSON, which live in the `org.renjin.gcc.gimple`
package.

While Gimple is quite easy-to-consume as compared with C++ or Fortran,
it's not a standardized language and is very much an internal part of GCC
and subject to change. 

So the first thing that can go wrong is that we are missing some new
or rarely-used [Gimple Operation](compiler/src/main/java/org/renjin/gcc/gimple/GimpleOp.java),
[GimpleStatement](compiler/src/main/java/org/renjin/gcc/gimple/statement/GimpleStatement.java) or
[GimpleExpr](compiler/src/main/java/org/renjin/gcc/gimple/expr/GimpleExpr.java)
which leads to a parsing exception.

It should be straightforward to add the additonal subclasses or properties
to the object model (See Jackson's [documentation](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations)). 
Then re-run the build and see what needs to be done in the actual
code generation.

Once all Gimple sources have been parsed, the `GimpleCompiler` class
takes over and the fun really starts :-)

## Preprocessing Record Types

First, we have to merge alot of things across compilation units. 

GCC considers each source file to be an independent compilation unit.
If two sources, include the same header file, each compilation unit
will get its own copy of `struct` definitions. In Gimple, both C `structs` 
and C++ classes map to "Records".

For GCC, this isn't problematic because it all comes down to bits: each
compilation unit is compiled into machine code that doesn't care whether 
a region of memory containing two doubles is called a "Point" or "Complex."

Since we are targeting the JVM, names _are_ very important, so we have
to merge similar `GimpleRecordTypeDefs` together before we go further.

We also have to decide on a way to represent each record type. The JVM
simply doesn't have an equivalent of heterogenous value types (at least [not yet](http://openjdk.java.net/projects/valhalla/))
so we have to play some tricks to handle them properly.

The best is when the record types are not actually heterogenus. For example,
for the C `struct point_t {double x; double y; }`, we can use a double
array in the JVM, which maps perfectly to the C struct.

... To be continued...