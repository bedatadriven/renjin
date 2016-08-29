
#include <stdio.h>

#include "assert.h"

void zero_out(void *p, int count) {
    memset(p, 0, count);
}

void test_double() {
    double a[3];
    a[0] = 41;
    a[1] = 42;
    a[2] = 43;
    
    zero_out(a, sizeof(double) * 2);
    
    ASSERT(a[0] == 0);
    ASSERT(a[1] == 0);
    ASSERT(a[2] == 43);
}

void test_double_ptr() {
    
    double* p[3];
    p[0] = malloc(sizeof(double) * 10);
    p[1] = malloc(sizeof(double) * 20);
    p[2] = malloc(sizeof(double) * 30);

    p[0][0] = 41;
    p[1][1] = 42;
    p[2][2] = 43;

    zero_out(p+1, sizeof(double*)*2);

    ASSERT(p[0][0] == 41);
    ASSERT(p[1] == NULL);
    ASSERT(p[2] == NULL);
}
