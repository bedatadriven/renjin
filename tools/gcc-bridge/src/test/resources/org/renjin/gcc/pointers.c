

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
