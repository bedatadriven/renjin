
#include <stdio.h>
#include <stdlib.h>

#include "assert.h"


void test_real96() {

    long double x = 1/3;
    long double y = 2/3;

    ASSERT( fabs((2.0*x) - y) < 0.00001);
}

void test_real96_pointers() {

    long double a[6];
    a[0] = 100;
    a[1] = 101;
    a[2] = 102;
    a[3] = 103;
    a[4] = 104;
    a[5] = 105;

    long double *pa = &a[0];
    long double *pb = &a[3];

    ASSERT(*pa == 100);
    ASSERT(*pb == 103);
    ASSERT(pb[2] == 105);
}