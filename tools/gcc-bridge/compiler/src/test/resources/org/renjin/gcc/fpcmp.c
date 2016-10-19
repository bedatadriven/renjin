
#include <math.h>
#include <float.h>

#include "assert.h"

int lessThan(double x, double y) {
  int i;
  if(x < y) {
    i = 1;
  } else {
    i = 0;
  }
  return i;
}

int flessThan(float x, float y) {
  int i;
  if(x < y) {
    i = 1;
  } else {
    i = 0;
  }
  return i;
}



int lessThanEqual(double x, double y) {
  int i;
  if(x <= y) {
    i = 1;
  } else {
    i = 0;
  }
  return i;
}

int greaterThan(double x, double y) {
  int i;
  if(x > y) {
    i = 1;
  } else {
    i = 0;
  }
  return i;
}


int greaterThanEqual(double x, double y) {
  int i;
  if(x >= y) {
    i = 1;
  } else {
    i = 0;
  }
  return i;
}

int truncate(double x) {
  int y = (int)x;
  return y;
}

void test() {

    ASSERT(greaterThanEqual(42, 0) == 1);
    ASSERT(greaterThanEqual(0, NAN) == 0);
    ASSERT(greaterThanEqual(2.5, 2.5) == 1);
}