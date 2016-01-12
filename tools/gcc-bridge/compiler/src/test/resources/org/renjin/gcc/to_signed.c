
#include <inttypes.h>

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

uint16_t 
castInt16ToUnsignedInt8