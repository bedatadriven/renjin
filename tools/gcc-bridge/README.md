
# GCC-Bridge

GCC-Bridge is a C, C++ and Fortran to Java bytecode compiler. 

GCC-Bridge uses GCC as a front end to generate Gimple, and then compile Gimple to a Java class file.

It is composed of a few components:

   * A [GCC Plugin](gcc-plugin): a C plugin for GCC that dumps the intermediate Gimple tree to a 
     JSON file during compilation.
   * A [Gimple Compiler](compiler), which compiles the Gimple dumped by the GCC Plugin to pure JVM class files.
   * The [Runtime](runtime), which provide a partial mapping and/or implementation of the C Standard Library 
     as well as a number of GCC builtins. 
   * A [Maven Plugin](maven-plugin) which makes it easy to compile C, C++ and Fortran sources within 
     a traditional Java project. 
     
For a first introduction to GCC Bridge, the blog post 
[Introducing GCC Bridge](http://www.renjin.org/blog/2016-01-31-introducing-gcc-bridge.html) may be helpful.

We're also making an effort to technical documentation up to date here. Topics currently include:

   * [Compilation Strategies](docs/Compilation.md)
   * [Linking](docs/Linking.md)
   
