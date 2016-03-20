

#include <math.h>


double Square(double x) {
  return x * x;
}


double cube(double x) {
  return x * x * x;
}

double sum_transform_array(double* values, int length, double (*fn)(double) ) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += (*fn)(values[i]);
  }
  return sum;
}

double sum_array(double *values, int length) {
  double (*fn)(double) = 0;
  if(length > 1) {
    fn = &Square;
  } else {
    fn = &cube;
  }
  return sum_transform_array(values, length, fn);
}

double exp_sum(double *values, int length) {
  return sum_transform_array(values, length, &exp);
}