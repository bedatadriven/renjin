
#include "link.h"

int magic_number1 = 420;

extern int shared_triple(int x);


// This function is private to the link1 compilation unit
static int get_unit_number() {
  return 1;
}

static int test() {
  return shared_triple(get_unit_number());
}

double sum_x(point *p1, point *p2) {
  return p1->x + p2->x;
}

extern int dummy1() {
  test();
  get_unit_number();
}