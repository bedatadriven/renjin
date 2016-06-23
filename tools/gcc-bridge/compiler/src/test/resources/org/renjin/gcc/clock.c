
#include <time.h>
#include "assert.h"

void test_realtime() {
    struct timespec tp;
    clock_gettime(CLOCK_REALTIME, &tp);
    
    // Unless the system clock is VERY wrong, time should have marched on...
    // 1466688500 = 2016-06-23 
    ASSERT(tp.tv_sec > 1466688500);   
    
}

void test_monotonic() {
    struct timespec start;
    struct timespec end;

    
    clock_gettime(CLOCK_MONOTONIC, &start);
    clock_gettime(CLOCK_MONOTONIC, &end);
    
    ASSERT( (end.tv_sec > start.tv_sec) || ((end.tv_sec == start.tv_sec) && (end.tv_nsec >= start.tv_nsec)));
    
}