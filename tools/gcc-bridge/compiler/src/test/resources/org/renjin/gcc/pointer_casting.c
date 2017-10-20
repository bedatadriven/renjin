

#include <stdlib.h>
#include <inttypes.h>

#include "assert.h"



void test_case1() {

    // Because x is used in an "addressOf" operation below,
    // we know that we have to store x in an array.
    // long x[1] = new long[] {234342342L };

    long x = 234342342L;

    // Because we know that
    long *px = &x;

    uint8_t *p = (uint8_t *)px;

    ASSERT( p[0] == 198 );
    ASSERT( p[1] == 199 );
    ASSERT( p[2] == 247 );
    ASSERT( p[3] == 13  );


}

