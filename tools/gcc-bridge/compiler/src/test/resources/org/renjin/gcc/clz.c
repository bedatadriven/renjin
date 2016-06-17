
#include "assert.h"
#include <inttypes.h>


int clzi(int x) {
    return __builtin_clz(x);
}

int clzb(uint8_t x) {
    return __builtin_clz(x);
}

int clzl(int64_t x) {
    return __builtin_clz(x);
}

void test_ints() {
  //  ASSERT(clzi(0) == 31)
    ASSERT(clzi(8) == 28)
    ASSERT(clzi(1024) == 21)
    ASSERT(clzi(16777215) == 8)
    ASSERT(clzi(-1) == 0)
}

void test_bytes() {
    ASSERT(clzi(3) == 30);
    ASSERT(clzi(255) == 24);
}