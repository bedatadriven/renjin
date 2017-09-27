
#include <math.h>
#include <inttypes.h>

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

int int32_to_uint8(int x) {
    unsigned char y = x;
    int z = y;
    return z;
}

int uint32_to_uint8(unsigned int x) {
    unsigned char y = x;
    int z = y;
    return z;
}

double uint64_to_double(uint64_t x) {
    return (double)x;
}

