
# GNU R Runtime

This module contains a set of Java classes that provide a compatability layer for packages written
for GNU R with C or C++ code targeting the C API of GNU R.

Each of the classes in [org.renjin.gnur](src/main/java/org/renjin/gnur) has been generated from a 
GNU R header file. We are gradually providing implementations that form a bridge between GNU R and 
Renjin. 

The signatures of the methods in this compatability layer are guaranteed to be stable, so that packages
compiled against this layer should not break as Renjin changes.