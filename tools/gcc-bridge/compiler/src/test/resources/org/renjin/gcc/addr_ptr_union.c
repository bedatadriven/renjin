
#include <stdio.h>
#include <stdlib.h>

#include "assert.h"

struct st { 
    void *p;
};

void init_p(double **pp) {
    double *pd = malloc(sizeof(double)*10);
    pd[0] = 42;
    
    *pp = pd;
}


void test() {
    struct st s;
    init_p(&(s.p));
    
    double *pd = s.p;
    ASSERT(pd[0] == 42)
}
