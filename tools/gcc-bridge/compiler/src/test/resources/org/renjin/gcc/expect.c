
#include "assert.h"


#include <stdio.h>

#define likely(x)    __builtin_expect((x), 1)
#define unlikely(x)  __builtin_expect((x), 0)


int is_negative(int x) {
    if(unlikely(x < 0)) {
        return -42;
    } else {
        return 42;
    }
}


void test_expect() {

    ASSERT(is_negative(1) == 42)
    ASSERT(is_negative(0) == 42)
    ASSERT(is_negative(-5) == -42)
}
