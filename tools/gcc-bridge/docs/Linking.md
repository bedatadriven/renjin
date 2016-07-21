
# Linking and GCC Bridge

Compiling a single, standalone C or Fortran program is one thing, but in many cases, what is interesting is 
linking several independent libraries together.

## Namespaces and Linking

First, it's helpful to walk through how different sources / libraries get compiled together in GCC/Native land. 

Each source file is compiled independently. If you compile a Fortran source file from the BLAS library
that uses the LSAME function, for example, it will simply generate an object file that references the
 unresolved symbol lsame_.  (Fortran function names are mangled with a trailing underscore)

```
$ gcc -c dgemm.f
$ nm dgemm.o
0000000000000000 T dgemm_
                 U lsame_
$ gcc -c lsame.f
$ nm lsame.o
0000000000000000 T lsame_
```

After compiling all the individual source files from a library, you normally archive the resulting
objects files into a single object file like libblas.a, for example. This is not much different than a jar file. 

When you finally compile a shared library or an executable that relies on libblas, then GCC will actually
perform the linking, mapping references in mlapack to dgemm, for example, to the symbol defined in libblas.a.
Note that there is no namespacing here, it's all just a sea of flat symbol names. If you defined your own dgemm_
function in mplapack, that symbol would be used instead of the one defined in libblas.a. 

GCC-Bridge follows an analogous procedure. The compilation phase is largely the same, but we enable our plugin
which dumps out the gimple for each source file:

```
$ gcc -c -m32 -fplugin=/path/to/bridge.so dgemm.f
```
This creates a dgemm.gimple file alongside the dgemm.o file. The two files contain largely the same information 
- just that Gimple is a much higher-level representation of the functions defined in dgemm.f than the
x86 machine code in dgemm.o.

The real work for us starts when the GCC Bridge GimpleCompiler runs, collects all the .gimple files, 
and compiles each function in the gimple to a JVM static method. 

JVM object code is of course organized differently, not into a table of symbols but into class files and their methods. 
Arbitrarily, GCC Bridge, generates one JVM class per source file, and a public
static method in that class for every function:

```
$ javap scabs1.class 
Compiled from "scabs1.f"
public class org.renjin.math.scabs1 {
  public static float scabs1_(float[], int);
}
```

The package here ("org.renjin.math") is also arbitrary and provided as option to GCC-Bridge.

GCC-Bridge is also performing a link-step during compilation. Just like the GNU Linker, 
it maps symbolic names to the location of the compile code. In the native case, the linking is done from name to an 
offset in the object file. In our case, we link the symbol name to a JVM class and method name. 

## Depending on GCC-Bridge Compiled Libraries

Of course, with GCC-Bridge we wanted to be able to link together libraries compiled separately in a manner 
that's at least analogous to how linking works in GCC-land. For this reason, GCC-Bridge also emits metadata 
for each compiled function.  If you open renjin-blas.jar from nexus.bedatadriven.com, you'll find a set of text 
files in META-INF/org.renjin.gcc.symbols that help GCC-Bridge map the raw symbol names (like 'dgemv_' for fortran) 
to the previously-compiled Java methods.

For example:
```
META-INF/org.renjin.gcc.symbols/dgemv_ 
```

Contains the  properties file:
```
type=METHOD
class=org/renjin/math/dgemv__
member=dgemv_
descriptor=(Lorg/renjin/gcc/runtime/BytePtr;...I)V
```

This means that you add compiled libraries just like any other Maven dependency:

```
<dependency>
  <groupId>org.renjin</groupId>
  <artifactId>renjin-blas</artifactId>
  <version>0.8.2087</version>
</dependency>
```

And then when you compile-link the mplapack sources with GCC-Bridge, it will use this metadata to find the 
compiled JVM method provided by renjin-blas.jar

