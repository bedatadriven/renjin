#include "assert.h"

struct A {
   int x;
   double y;
};

union B {
  int *pi;
  struct A *pa;
};

void test_rup() {
  struct A a;
  a.x = 41;
  a.y = 1.2;

  union B b;
  b.pa =  &a;

  ASSERT(b.pa->x == 41);
  ASSERT(b.pa->y == 1.2);
}

void test_intptr() {
  int i = 44;
  union B b;
  b.pi = &i;

  ASSERT( *(b.pi) == 44);
}