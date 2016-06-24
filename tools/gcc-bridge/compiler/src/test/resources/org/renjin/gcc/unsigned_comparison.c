
#include "assert.h"
#include <inttypes.h>

void test_uint32_comparison() {
    uint32_t x = 0xFFFFFFFF;
    
    ASSERT(x > 0);
    ASSERT(x > 100);
    ASSERT(0 < x);
}

void test_uint8_comparison() {
    uint8_t x = 205;
    
    ASSERT(x > 0)
    ASSERT(x == 205)
    ASSERT(x >= 205)
    ASSERT(x <= 205)
    ASSERT(x <= 220)
    ASSERT(x < 0xFF)
    ASSERT(x < 1000)
    
    uint8_t y = 220;
    ASSERT(x < y)
    ASSERT(y > x)
    ASSERT(x != y)
}

void test_uint16_comparison() {
    uint16_t x = 32799;
    
    ASSERT(x > 0)
    ASSERT(x < 33000)
}