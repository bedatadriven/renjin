
#include <time.h>
#include "assert.h"

void test_gettime() {
    struct timespec tp;
    clock_gettime(CLOCK_REALTIME, &tp);
    
    ASSERT(tp.tv_sec > 0);
    ASSERT(tp.tv_nsec > 0);
}