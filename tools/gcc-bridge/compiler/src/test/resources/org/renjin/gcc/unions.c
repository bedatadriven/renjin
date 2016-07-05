
#include "assert.h"
#include <inttypes.h>
#include <stdio.h>

// This should be represented as an array of bytes.
// because we can convert a uint8_t[4] to uint_32 and back.
union u1 {
   uint32_t a;
   uint8_t b[4];
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
