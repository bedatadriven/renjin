
# GCC-Bridge

GCC-Bridge is a C, C++ and Fortran to Java bytecode compiler. 

GCC-Bridge uses GCC as a front end to generate Gimple, and then compile Gimple to a Java class file.

## Native to Virtual Machine Mapping


### Functions

C Functions are translated as public static methods. 


### Pointers

Within function bodies, pointer variables are mapped to two local variables: an array variable, and an offset 
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

### Returning Pointers

When pointers are returned from a function, they must be first wrapped as `Ptr` instance as JVM methods can 
only return a single value.

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
  double x_array = malloc(sizeof(double) * n)
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



### Global Variables

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

### Global Pointer Variables

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

### Strings and String Handling

The C `char` type is actually a string of bytes under the hood, and so GCC-Bridge represents all C strings as 
`byte` arrays under the hood. 


### Addressable Pointer local variables

Prior to code generation, we determine which local variables need to be "adressable.".

C:

```
int main {
  double *x;
  init_vector(&x);  
  return x[0] + x[1];
}

void init_vector(double **x) {
  double *y = malloc(sizeof(double) * 2);
  y[0] = 41;
  y[1] = 42;
  *x = y;
}
```

Java:

```
public static int main() {
  DoublePtr ptr = new DoublePtr(null, 0);
  init_vector(ptr);
  return ptr.get(0) + ptr.get(1);
}

public static void init_vector(DoublePtr ptr) {
  double y[] = new double[2];
  int y$offset = 0;
  y[y$offset + 0] = 41;
  y[y$offset + 1] = 42;
  ptr.array = y;
  ptr.offset = y$offset;
} 
```

### Structs:

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

public class Main{

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

Java the language doesn't actually have a way of passing a method directly as a `MethodHandle`, but 
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

