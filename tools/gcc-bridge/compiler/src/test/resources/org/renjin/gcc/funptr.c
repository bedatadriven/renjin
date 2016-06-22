

#include <math.h>
#include "assert.h"


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



void test_sum_array() {
    double x[] = {1, 4, 16};
    double result = sum_array(x, 3);
    
    ASSERT(result == 273)
}

int is_cube_fn(void *fn) {
    return fn == &cube;
}

int is_square_fn(void *fn) {
    void *p = &Square;
    return p == fn;
}

void test_comparison_with_void_ptr() {
    ASSERT(is_cube_fn(&cube));
    ASSERT(!is_cube_fn(&Square));
    
    ASSERT(!is_square_fn(&cube))
    ASSERT(is_square_fn(&Square))
}