

#include <stdlib.h>

#include "assert.h"

void test() {
    void* p = 0;
    int x = compute(p, 41);
    ASSERT(x == 82);
}

int compute(double *px, int x) {
    if(x > 100) {
        return *px * x;
    } else {
        return x * 2;
    }
}