

#include <math.h>


double sum_x_array(double* values, int length, double (*fn)(double) ) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += fn(values[i]);
  }
  return sum;
}

double sum_array(double *values, int length) {
  sum_x_array(values, length, &sqrt);
}