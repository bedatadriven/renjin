#include <math.h>
#include "assert.h"

void test_signbit() {
    ASSERT(!signbit(4));
    ASSERT( signbit(-4.3));
    ASSERT( signbit(-4));
    ASSERT(!signbit(-0));
}