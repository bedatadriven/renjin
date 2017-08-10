
#include "assert.h"
#include <stddef.h>


void assert_div_32(size_t x, size_t y, size_t z) {
    size_t actual = (x/y);
    printf("actual = %X\n", actual);
    ASSERT(actual == z);
}

void test_unsigned_div() {
    assert_div_32((size_t)-1, 8, 0x1FFFFFFF);
}