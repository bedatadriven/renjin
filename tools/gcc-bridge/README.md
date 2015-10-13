
# GCC-Bridge

GCC-Bridge is a C, C++ and Fortran to Java bytecode compiler. 

GCC-Bridge uses GCC as a front end to generate Gimple, and then compile Gimple to a Java class file.

## Native to Virtual Machine Mapping


### Functions

C Functions are translated as public static methods. 


### Pointer Local Variables

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

```
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
```

Java:

```
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
    return(pointer);
}
```

