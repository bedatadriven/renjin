
#include <inttypes.h>
#include <stdlib.h>

int dbl_memcmp(double x, double y) {
    return memcmp(&x, &y, sizeof(double));
}

int long_memcmp(int64_t x, int64_t y) {
    return memcmp(&x, &y, sizeof(int64_t));
}