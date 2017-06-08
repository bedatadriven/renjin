
#include <stdio.h>
#include <inttypes.h>

#include "assert.h"

uint8_t downcast_u8(uint64_t x) {
    uint8_t y =  (uint8_t)x;
    return y;
}

int8_t downcast_8(uint64_t x) {
    int8_t y =  (int8_t)x;
    return y;
}

int16_t downcast_16(uint64_t x) {
    int16_t y =  (int16_t)x;
    printf("casting down to u16 %X -> %d\n", x, y);
    return y;
}

uint16_t downcast_u16(uint64_t x) {
    uint16_t y =  (uint16_t)x;
    printf("%d\n", y);
    return y;
}


void testu_8() {
    ASSERT(downcast_u8(0) == 0);
    ASSERT(downcast_u8(12) == 12);
    ASSERT(downcast_u8(0xFF) == 0xFF);
    ASSERT(downcast_u8(0xFFF) == 0xFF);
    ASSERT(downcast_u8(0xffffffffffffffff) == 0xFF);
}

void test_8() {
    ASSERT(downcast_8(0) == 0);
    ASSERT(downcast_8(0xF) == 15);
    ASSERT(downcast_8(0xFE) == -2);
    ASSERT(downcast_8(0xFF) == -1);
    ASSERT(downcast_8(0xFFF) == -1);
    ASSERT(downcast_8(0xffffffffffffffff) == -1);
}


void test_16() {
    ASSERT(downcast_16(0) == 0);
    ASSERT(downcast_16(0xF) == 15);
    ASSERT(downcast_16(0xFE) == 254);
    ASSERT(downcast_16(0xFF) == 255);
    ASSERT(downcast_16(0xFFF) == 4095);
    ASSERT(downcast_16(0xFFFF) == -1);
    ASSERT(downcast_16(0xffffffffffffffff) == -1);
}


void test_u16() {
    ASSERT(downcast_u16(0) == 0);
    ASSERT(downcast_u16(0xF) == 15);
    ASSERT(downcast_u16(0xFE) == 254);
    ASSERT(downcast_u16(0xFF) == 0xFF);
    ASSERT(downcast_u16(0xFFF) == 0xFFF);
    ASSERT(downcast_u16(0xFFFF) == 0xFFFF);
    ASSERT(downcast_u16(0xffffffffffffffff) == 0xFFFF);
}

