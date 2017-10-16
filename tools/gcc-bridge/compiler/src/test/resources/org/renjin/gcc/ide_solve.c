
#include <stdlib.h>
#include <math.h>
#include "assert.h"

// The idea is that we should be able to
// deduce that px is always backed by a double[] array,
// so we don't need to use the whole vptr artifice

static double * gen_pointer() {
    double *px = malloc(sizeof(double)*10);
    px[0] = 91;
    px[1] = 92;
    px[2] = 93;
    double *py = px;
    py[3] = 94;
    py[4] = 95;
    py[5] = 96;
    return px;
}

void test_receive_pointer() {
    double *px = gen_pointer();
    ASSERT(px[0] == 91);
}