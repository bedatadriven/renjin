

#include <math.h>


double sum_array(double* values, int length) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += values[i];
  }
  return sum;
}