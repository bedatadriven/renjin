
#include "link.h"

extern int magic_number1;


// This function is private to the link2 compilation unit
static int get_unit_number() {
  return 2;
}

static int test() {
  return get_unit_number();
}


extern int shared_triple(int x) {
  return x * 3;
}

static int test_global_var() {
  return magic_number1;
}

extern double test_points() {
  point a, b;
  a.x = 40;
  b.x = 1;
  return sum_x(&a, &b);
}