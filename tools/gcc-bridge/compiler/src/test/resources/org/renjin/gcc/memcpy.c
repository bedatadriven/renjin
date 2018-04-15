
#include <stdlib.h>
#include "assert.h"

int test_memcpy() {
  char x[10];
  strcpy(x, "foobar");
  if(x[3] == 'b') {
    return 1;
  } else {
    return 0; // failed
  }
}

void test_ptr_memmove() {

  char **pp[5];
  pp[0] = "A";
  pp[1] = "B";
  pp[2] = "C";
  pp[3] = "D";
  pp[4] = "E";

  memmove(pp, pp+1, sizeof(char*)*4);

  ASSERT(strcmp(pp[0], "B") == 0);
  ASSERT(strcmp(pp[1], "C") == 0);
  ASSERT(strcmp(pp[2], "D") == 0);
  ASSERT(strcmp(pp[3], "E") == 0);
  ASSERT(strcmp(pp[4], "E") == 0);

}