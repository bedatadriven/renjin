
# Compilation

The essential model underlying C and Fortran is wondrously simple: there is a big bag of bytes out there 
called 'memory', numbered from 1 to 4GB in which everything lives, from variables on the stack to 
arrays allocated with `malloc()`.

In this model, a pointer is simply the offset within this giant bag of bytes. Want to cast a `double*` to
a `uint8_t*`? No problem! Nothing actually changes, it's still the same number pointing to the same offset in
our bag of bytes, it only changes how the fussy C compiler understands operations like + or -.

The Java Virtual Machine (JVM) does not share this beautifully parsimonious model. There is the stack, and there
is the heap, and they are Different, and Not To Be Confused. An array of integers, can never, ever, be cast 
to an array of doubles. That would be madness.

For this reason, it is most likely theoretically impossible to compile any given, arbitrary C program to 
JVM bytecode. Fortunately, I am not a Computer Scientist and am largely uninterested in theory. In practice, 
there is a large and very useful class of C programs which _can_ be compiled to JVM bytecode, though some require
more creativity on our part than others.

The following document provides a rough overview of how certain aspects of C programs are "translated" to
the JVM.

## Functions

C Functions are translated as public static methods. 


## Pointers

Within function bodies, pointer variables are mapped to two local JVM variables: an array variable, and an offset 
variable.

C: 

```
double sum(double *x, int n) {
  int i;
  double s = 0;
  for(i=0; i<n;++i) {
    s += *x;
    x++;
  }
  return s;
}
```

Java (equivalent):

```
public static double sum(DoublePtr x, int n) { 
  double x_array[] = x.array;
  double x_offset = x.offset
  int i;
  double s = 0;
  for(i=0;i<n;++i) {
    s += x_array[x_offset];
    x_offset++;
  }
  return s;
}
```

## Returning Pointers

Since we cannot return more than one value from a JVM method, we have to wrap our array and offset values 
into a JVM object. We have defined a series of "Fat Pointer" classes in the `gcc-runtime` module for this 
purpose, which includes `IntPtr`, `DoublePtr`, `LongPtr`, ... `ObjectPtr`.

C: 

```
double* rnorm(int n) {
  double *x = malloc(sizeof(double) * n)
  int i;
  for(i=0; i<n;++i) {
    x[i] = /* calc */;
  }
  return s;
}
```

Java:
```
public static DoublePtr rnorm(int n) {
  double[] x_array = new double[n];
  int x_offset = 0;
  int i;
  for(i=0; i<n;++i) {
    x_array[x_offset + i] = /* calc */;
  }
  return new DoublePtr(x_array, x_offset);
}
```

### Pointers to Pointers

Beyond one level of indirection we don't specialize: 

C:

```.c

static double** global_matrix;

double **dmatrix(double *x, int ncol, int nrow) {

  int i;
  double **pointer;

  pointer = (double **) ALLOC(nrow, sizeof(double *));
  for (i=0; i<nrow; i++) {
      pointer[i] = x;
      x += ncol;
  }
  return(pointer);
}

void init_matrix() {
  double data[] = {1, 2, 3, 4};
  global_matrix = cmatrix(data, 2, 2);
}

```

Java:

```.java

public static DoublePtr[] global_matrix;
public static int global_matrix$offset;

public static ObjectPtr dmatrix(DoublePtr x, int ncol, int nrow) {

  double[] x_array = x.array
  int x_offset = x.offset
  
  int i;
  DoublePtr[] pointer_array;
  int pointer_offset = 0;

  pointer =  new DoublePtr[nrow];
  for (i=0; i<nrow; i++) {
      pointer_array[pointer_offset+i] = new DoublePtr(x_array, x_offset);
      x_offset += ncol;
  }
  
  return new ObjectPtr(pointer_array, pointer_offset);
}

public static void init_matrix() {
  double data[] = {1,2,3,4};
  ObjectPtr tmp = cmatrix(data, 2, 2);
  global_matrix = (DoublePtr[])tmp.array;
  global_matrix$offset = tmp.index;
}

```

## Global Variables

Global variables are compiled as static public fields. 

```
int global_var;

void main() {
   global_var = 42;
}
```

Java:

```
public static class MainClass {

  public static int global_var;
  
  public static void main() {
    global_var = 42;
  }
}
```

## Global Pointer Variables

Global variables pointers are compiled as two static fields: one for the backing
array, and one for the offset.


```
int *global_var;

void main() {
  global_var = malloc(sizeof(int));
  init(global_var);
}

void init(int *x) {
  *x = 42;
}
```

```
public class Main {

  public static int[] global_var;
  public static int global_var$offset;
  
  public static void main() {
    // global_var = malloc(sizeof(int))
    global_var = new int[1];
    global_var$offset = 0;
  
    // init(global_var)
    init(new IntPtr(global_var, global_var$offset))
  }
  
  public static void init(IntPtr x) {
    // *x = 42
    x.array[x.offset] = 42;  
  }
}
```

## Strings and String Handling

The C `char` type is actually a single byte under the hood, and so GCC-Bridge represents all C strings as 
`byte` arrays. The `BytePtr` class contains a few helper methods for creating Java String objects from a 
 null terminated `BytePtr` instance, and vice versa.


## Addressable Local variables

Since in C world the stack is just one specific part of our big bag of bytes, there is nothing special
about getting the address of a local variable and passing that address to some random function which will then
make changes.

In the JVM world such a thing is, by design, completely unthinkable.

So we have to be creative. Prior to code generation, we determine which local variables need to be "addressable" 
by searching the Gimple AST for address-of expressions which feature a local variable. 

For local variables which are tagged as "addressable", we will actually store in an array of length one, which
the JVM allocates on the heap. We can then "address" this value by 

C:

```
int main {
  double x;
  init_vector(&x);  
  return x + 1;
}

void init_vector(double *x) {
  *x = 42;
}
```

Java:

```
public static int main() {
  double x[] = new double[1];
  init_vector(new DoublePtr(x, 0));
  return x[0] + 1;
}

public static void init_vector(DoublePtr x) {
  double[] x$array = x.array;
  int x$offset = x.offset;
  x$array[x$offset] = 42;
}
```

## Structs:

C Structs are probably the most difficult part of compiling "native" code to the JVM. In C land,
structs are just bags of contiguous bytes. Field names and types melt away to nothing as C is compiled down to machine
code, and as a consequence, they're not treated very seriously in C: you can happily cast a record to an array
of integers, or bytes, or whatever is convenient, and it's on you as the C programmer to be sure that the result
means something.

The closest analog in JVM land, however, are classes, and there, by constrast, field naems and types are _sacred_, 
which makes them a very poor fit for C structures.

For these reasons, we have to be creative and use different strategies for struct types, or "record types" as they
are called in Gimple, depending on how they are used.

This is an area in active development, so take a look at the source code to see how things are actually managed 
at present, but here is a general overview.

### RecordArrayTypeStrategy

The easiest case to handle is structs whose fields all have the same primitive type, for example:

```
struct point {
    double x;
    double y;
}
```

In this case, we can compile all variables and fields of such a type to simply arrays of doubles. This is great,
because we can happily support casting to `double*` types, and, just like our friends in C land, we incur no
overhead in allocating the individual point structures in an array. We can happily compile something like:

```
struct point *pp = malloc(sizeof(struct point) * 1000);
```
as
```
double [] pp = new double[1000];
```

as opposed to 

```
Point[] pp = new Point[1000];
for(int i = 0; i<1000;++i) {
  pp = new Point();
}
```

which is catastrophically inferior to the C case in terms of performance. 

### Empty Structs

An even easier case, actually, is empty structs. This is pretty common in C++, where many C++ classes are
just there as syntactical sugar and don't actually contain any data. However, for various reasons, even 
empty structures need to have their own unique address, so we can't get away with ignoring them completely. 

GCC Bridge compiles structs without fields as simply an instance of `java.lang.Object`. This assures that the
struct has an address, but makes it easy to cast the struct to any other type.

### RecordClassStrategy

But life is not all roses, and `struct`s with fields of different types are hardly uncommon. If we can't represent
an a `struct` as an array, then we have to fall back to representing as a full-fledge Java class. 

In the simplest of cases this turns out ok:


C:
```
typedef struct {
   int years_open;
   int plan_type;
} account;

double account_value(account * pa) {
  if(pa->plan_type == 2) {
    return pa->years_open * 2500;
  } else {
    return pa->years_open * 1500;
  }
}

double test_account_value() {
  account ac;
  ac.years_open = 2;
  ac.plan_type = 2;

  return account_value(&ac);
}
```

Java:
```

public class Main {

  public static class account{
    public int years_open;
    public int plan_type;
  }

  public static int account_value(account pa){
    if(pa.plan_type == 2){
      return pa.years_open * 2500;
    } else {
      return pa.years_open * 1500;
    }
  }

  public static int test_account_value(){
    account ac = new account();
    ac.years_open = 2;
    ac.plan_type = 2;
    return account_value(ac);
  }

}

```

### Function Pointers

Pointers to functions are compiled as JVM [MethodHandle](https://docs.oracle.com/javase/7/docs/api/java/lang/invoke/MethodHandle.html)
references, which is actually a pretty good fit for function pointers.


C:

```.c
void transform_array(double *x, int length, double (*fn)(double) ) {
  int i;
  for(i=0;i<length;++i) {
    x[i] = fn(x[i]);
  }
}

double cube(x) {
  return x * x * x;
}

void main() {
  double x[2];
  x[0] = 2;
  x[1] = 3; 
  transform_array(&x, 2, &cube);
}
```

Java:
```.java
public class MainClass {

  public static void transform_array(DoublePtr x, int length, MethodHandle fn) {
    int i;
    for(i=0;i<length;++i) {
      x.array[x.offset + i] = fn.invokeExact(x.array[x.offset + i];
    }
  }
  
  public static double cube(double x) {
    return x * x * x;
  }
  
  public static void main() {
    double x[2] = new double[2];
    x[0] = 2;
    x[1] = 3;
    transform_array(x, 2, /*  MethodHandle invokestatic cube:(D)D */)
  }
}
```

Java 7 the language doesn't actually have a way of passing a method directly as a `MethodHandle`, 
(we have to wait for Java 8 for this) but 
the JVM itself allows us to store a `MethodHandle` in the constant pool and push it onto the stack
using the `LDC` instruction.


### Pointers to Pointers

```.c
// allocate a 10x100 matrix
double** cmatrix() {
  double ** m = malloc(10 * sizeof(double*))
  int row;
  for(row=0;row<10;++i) {
    m[i] = malloc(100 * sizeof(double)))
  }
  return m;
}
```

```.java
// allocate a 10x100 matrix
cmatrix() {
  double [][] m;
  int m$offset;
  int[] m$$offsets;
  
  m = new double[][10];
  m$offset = 0;
  
  for(int row=0;row<10;++row) {
    m[row] = new double[100];
    m$$offsets[row] = 0;
  }
}
```