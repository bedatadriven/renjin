#include <stdio.h>
#include <stdlib.h>

#include "assert.h"

void * my_alloc(int size) {
    return malloc(size);   
}


void init_double(void *p) {
    
    double *pd = (double *) p;
    pd[0] = 3.145;
    pd[1] = 42.0;
}

void test_double() {

    void * pv = my_alloc( sizeof(double) * 2 );

    init_double(pv);
    
    double * pd = (double*)pv;
    
    ASSERT(pd[0] == 3.145)
    ASSERT(pd[1] == 42.0)
}


void* indirect_call(void* (*fn)(int)) {
   return fn(16);
}

void test_malloc_funptr() {
  int *p = indirect_call(&malloc);
  p[0] = 91;
  p[1] = 92;
  p[2] = 93;
  p[3] = 94;
  
  ASSERT(p[0] == 91)
  ASSERT(p[1] == 92)
  ASSERT(p[2] == 93)
  ASSERT(p[3] == 94)
}