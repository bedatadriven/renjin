
#include "link.h"

// Declared elsewhere
extern int magic_number1;

extern int addressable_magic_number;


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

extern int test_global_var() {
  return magic_number1;
}

int quadruple(int *x) {
    int y = *x;
    return y * 4;
}

extern int test_addressable_global_var() {
    return quadruple(&addressable_magic_number);
}

extern double test_points() {
  point a, b;
  a.x = 40;
  b.x = 1;
  return sum_x(&a, &b);
}

extern int dummy2() {
  test();
  get_unit_number();
}