#include <stdio.h>
#include <complex.h>
#include <math.h>

#include "assert.h"

void test_divide() {

    double complex z1 = 1.0 + 3.0 * I;
    double complex z2 = 1.0 - 4.0 * I;


    char buf[1024];

    double complex quotient = z1 / z2;
    sprintf(buf, "The quotient: Z1 / Z2 = %.2f %+.2fi", creal(quotient), cimag(quotient));

    puts(buf);
    puts("\n");

    ASSERT( strcmp(buf, "The quotient: Z1 / Z2 = -0.65 +0.41i") == 0 )
}