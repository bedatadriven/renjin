
#include "assert.h"

#include <stdio.h>


void test_fopen() {

  FILE *fp = fopen("/dev/null","rb");

  ASSERT(fp == 0);
}
