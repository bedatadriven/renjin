
#include "assert.h"

#include <stdlib.h>

struct A {
  double x;
  double y;
  int z[];
};


void test_malloc() {

  struct A *pa = malloc(sizeof(struct A));
  pa->x = 41;
  pa->y = 46;
  
  struct A a = *pa;
  
  ASSERT(a.x == 41);
  ASSERT(a.y == 46);
}

