
#include <inttypes.h>

#include "assert.h"

int charToUnsigned16(char c) {
    uint16_t d = (uint16_t)c;
    return (int)d;
}

int int16ToUnsigned32(short s) {
    uint32_t d = (uint32_t)s;
    return (int)d;
}

int8_t uint32ToInt8(uint32_t x) {
    int8_t y = x;
    
    return y;

}

int16_t uint32ToInt16(uint32_t x) {
    int16_t y = x;
    return y;
}

uint16_t uint32ToUint16(uint32_t x) {
    uint16_t y = x;
    return y;
}

uint64_t uint32ToUint64(uint32_t x) {
    uint64_t y = x;
    return y;
}

void test_casting() {

    ASSERT(charToUnsigned16(0) == 0);
    ASSERT(charToUnsigned16(120) == 120);
    ASSERT(charToUnsigned16(-62) == 65474);

    ASSERT(int16ToUnsigned32(0) == 0);
    ASSERT(int16ToUnsigned32(4096) == 0x1000);
    ASSERT(int16ToUnsigned32(-34) == 0xffffffde);
    ASSERT(int16ToUnsigned32(-4142) == 0xffffefd2);

    ASSERT(uint32ToInt8(0) == 0);
    ASSERT(uint32ToInt8(3000) == -72);
    ASSERT(uint32ToInt8(4096) == 0);
    ASSERT(uint32ToInt8(0xffffffff) == -1);
    ASSERT(uint32ToInt8(0xfffffffe) == -2);

    ASSERT(uint32ToInt16(0) == 0);
    ASSERT(uint32ToInt16(0xbb8) == 3000);
    ASSERT(uint32ToInt16(0x40000) == 0);

    ASSERT(uint32ToUint16(0) == 0);
    ASSERT(uint32ToUint16(0x402) == 1026);
    ASSERT(uint32ToUint16(0x1000) == 4096);
    ASSERT(uint32ToUint16(0xFFFF) == 65535);
    ASSERT(uint32ToUint16(0x10003) == 3);

    ASSERT(uint32ToUint64(0) == 0x0);
    ASSERT(uint32ToUint64(0xFF) == 0xFF);
    ASSERT(uint32ToUint64(0xFFFFFFFF) == 0xFFFFFFFFL);

    ASSERT(uint32ToUint64(0) == 0);
    ASSERT(uint32ToUint64(0xFF) == 0xFFL);
    ASSERT(uint32ToUint64(0xFFFFFFFF) == 0xFFFFFFFFL);
}

void main() {
    test_casting();
}