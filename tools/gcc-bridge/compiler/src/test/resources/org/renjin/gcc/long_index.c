
#include "assert.h"
#include <inttypes.h>

int access(uint64_t j) {

    int a[3];
    a[0] = 100;
    a[1] = 101;
    a[2] = 102;

    return a[j];
}

void test_long_index() {

    ASSERT(access(2) == 102);
}