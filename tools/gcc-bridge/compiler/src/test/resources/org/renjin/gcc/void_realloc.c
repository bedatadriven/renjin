
#include <stdlib.h>
#include "assert.h"

void test_malloc() {

    void * p1 = malloc(sizeof(double)*3);
    void * p2 = realloc(p1, sizeof(double)*4);

    check_double(p2);
}

void check_double(void *p) {

    double *pd = (double*)p;

    ASSERT(pd[3] == 0);
}
