

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
