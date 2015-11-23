
#include <stdlib.h>
#include <math.h>


double sum_array(double* values, int length) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += values[i];
  }
  return sum;
}

void fill_array(double* values, int length) {
  int i;
  for(i=0;i!=length;++i) {
    values[i] = i * 1.54;
  }
}

double malloc_test() {
  double * x = malloc(sizeof(double)*100);
  fill_array(x, 100);
  return sum_array(x, 100);
}

double *global_var;


void malloc_global_test() {
  global_var = malloc(sizeof(double)*100);
  fill_array(global_var, 100);
}

double malloc_global_test2() {
 return sum_array(global_var, 100);
}

double* zero_array() {
  int need = 10;
  global_var = malloc(need * sizeof(double));
  int i;
  for(i=0;i<need;++i) {
    global_var[i] = 0.0;
  }
  return global_var;
}


double realloc_test() {
  double *x = malloc(2 * sizeof(double));
  x[0] = 41;
  x[1] = 42;
  double *y = realloc(x, 4 * sizeof(double));
  y[2] = 43;
  y[3] = 44;
  
  return sum_array(y, 4);
}
