
#include "assert.h"
#include <inttypes.h>
#include <stdio.h>

// This should be represented as an array of bytes.
// because we can convert a uint8_t[4] to uint_32 and back.
union u1 {
   uint32_t a;
   uint8_t b[4];
};

struct _dcomplex {
   double r;
   double i;
};

union iful {
    uint8_t u8;
    int8_t s8;
    uint16_t u16;
    int16_t s16;
    uint32_t u32;
    int32_t s32;
    float flt;
    double dbl;
    struct _dcomplex cpx;
};

void test() {

    union u1 x;
    
    x.a = 165795510;
    
    ASSERT(x.b[0] == 182);
    ASSERT(x.b[1] == 214);
    ASSERT(x.b[2] == 225);
    ASSERT(x.b[3] == 9);
    
    
    x.b[0] = 31;
    x.b[1] = 255;
    x.b[2] = 9;
    x.b[3] = 101;
    
    ASSERT(x.a == 1695153951);
}

// Not yet working
#ifdef WORKING_BETTER
void test_iful() {
    
    union iful iful;
    
    iful.dbl = 0.5612552;
    ASSERT(iful.u8 == 0x000000d2)
    ASSERT(iful.s8 == 0xffffffd2)
    ASSERT(iful.u16 == 0x0000b7d2)
    ASSERT(iful.s16 == 0xffffb7d2)
    ASSERT(iful.u32 == 0x7716b7d2)
    ASSERT(iful.s32 == 0x7716b7d2)
}
#endif