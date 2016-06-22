
#include "assert.h"

void test_long_double_arrays() {
    long double a[10];
    long double *pa = &a;
    
    int i;
    for(i=0;i<10;++i) {
        *pa = (long double)i;
        pa++;
    }
    
    pa = &a;
    
    ASSERT(a[0] == 0);
    ASSERT(a[1] == 1);
    ASSERT(*(pa+9)== 9);
    ASSERT(a[9] == 9);
}