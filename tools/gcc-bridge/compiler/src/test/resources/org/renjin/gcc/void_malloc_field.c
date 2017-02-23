
#include <stdio.h>
#include "assert.h"

struct S {
    int count;
    void * data;
};


void test_assign_malloc_thunk_field() {

    struct S s;
    s.data = malloc(sizeof(double)*10);

    double *x = *(s.data);
    x[0] = 41;
    x[1] = 42;

    ASSERT(x[0] == 41);

    double *y = malloc(sizeof(double)*10);
    y[0] = 91;
    y[1] = 92;

    s.data = y;
}