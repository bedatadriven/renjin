
#include <stdio.h>
#include <stdlib.h>

#include "assert.h"

struct st { 
    void *p;  // = new Object[1]
};

void init_p(double **pp) {
    double *pd = malloc(sizeof(double)*10);
    pd[0] = 42;
    
    // Object p = pp[0]
    // 
    *pp = pd;
}


void test() {
    struct st s;
    init_p(&(s.p));
    
    double *pd = s.p;
    ASSERT(pd[0] == 42)
}

// Now with union, which is effectively the same thing

union su {
    int *pi;
    double *pd;
};

void test_union() {
    union su u;
    init_p(&u.pd);
    
    double *pd = u.pd;
    ASSERT(pd[0] == 42)
}