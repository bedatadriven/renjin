#include <stdlib.h>
#include "assert.h"


void test_seed() {

    int x[3];

    srand(40039);
    x[0] = rand();
    x[1] = rand();
    x[2] = rand();

    srand(40039);
    ASSERT(rand() == x[0]);
    ASSERT(rand() == x[1]);
    ASSERT(rand() == x[2]);
}