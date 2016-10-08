
#include <stdio.h>
#include "assert.h"

int red_counter() {
  static int counter = 10;
  counter = counter + 1;
  return counter;
}

int blue_counter() {
  static int counter = 20;
  counter = counter + 1;
  return counter;
}

void test_static_var() {

  ASSERT(red_counter() == 11);
  ASSERT(blue_counter() == 21);

  ASSERT(red_counter() == 12);
  ASSERT(red_counter() == 13);

  ASSERT(blue_counter() == 22);
  ASSERT(blue_counter() == 23);
}
