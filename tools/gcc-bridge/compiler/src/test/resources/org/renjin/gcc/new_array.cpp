
#include <new>
#include "assert.h"

extern "C" void test_new_array() {

    double *pd = new double[3];
    pd[0] = 1.0;
    pd[1] = 1.1;
    pd[2] = 1.2;

    double first = *pd;
    double second = *(pd+1);
    double third = *(pd+2);

    ASSERT(first == 1.0);
    ASSERT(second == 1.1);
    ASSERT(third == 1.2);
}