
#include <stdlib.h>
#include <math.h>
#include <inttypes.h>
#include "assert.h"

// Here we expect two stack allocations
// And to see that p could point to either one.
// Therefore, p must be represented by a VPtr because it
// could either point to the double array x or an int array y.

uint8_t* stack_pointers() {
    double x = 3.145;
    int y = 99;

    int i=0;
    uint8_t *p;

    for(i=0;i<4;++i) {
        if(i % 2 == 0) {
            p = &x;
        } else {
            p = &y;
        }
        p[i] = 0;
    }

    return p;
}
