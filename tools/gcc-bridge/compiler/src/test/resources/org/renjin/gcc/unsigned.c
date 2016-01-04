
#include <math.h>

// from the bitops package
double bitflip(double a) {
    // in case of a negative, cast twice;
    int xbitWidth = 32;
    unsigned int mask = ( unsigned int ) -1 >> (32 - xbitWidth);
    unsigned int tmp = a < 0 ? (int) a : (unsigned) a;
    return (double) ( ~tmp & mask ) ;
}

double bitand(double x, double y) {
    if(logb(x) > 31 || logb(y) > 31) {
        return NAN;
    } else {
        return (double) ( (unsigned int) x & (unsigned int ) y ) ; 
    }
}

double unsignedIntRoundTrip(double z) {
    unsigned int y = (unsigned int)z;
    double z2 = y;
    return z2;
}