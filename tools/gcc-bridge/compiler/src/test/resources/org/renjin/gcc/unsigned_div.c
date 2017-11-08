
#include "assert.h"
#include <stddef.h>
#include <inttypes.h>


void assert_div_32(size_t x, size_t y, size_t z) {
    size_t actual = (x/y);
    printf("actual = %X\n", actual);
    ASSERT(actual == z);
}

void test_unsigned_div() {
    assert_div_32((size_t)-1, 8, 0x1FFFFFFF);
}

uint8_t do_neg(uint8_t x) {
    return -x;
}

void test_unsigned_neg() {
    ASSERT(do_neg(128) == 0x80);
    ASSERT(do_neg(255) == 0x01);
    ASSERT(do_neg(64) == 0xC0);
}

void main() {
    test_unsigned_neg();
}