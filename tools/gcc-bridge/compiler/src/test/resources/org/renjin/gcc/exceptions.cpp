
#include "assert.h"

struct NegativeException {};

int throwing_function(int x) {
  if(x < 0) {
    throw NegativeException();
  } 
  return x * 2;
}

int catching_function(int x) {
  try {
    return throwing_function(x);
  } catch(NegativeException e) {
    return -1;
  }
}

extern "C" void test() {
  ASSERT(catching_function(42) == 84);
  ASSERT(catching_function(-42) == -1);
}