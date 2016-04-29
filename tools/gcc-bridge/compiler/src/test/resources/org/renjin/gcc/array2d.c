

#include "assert.h"

void test() {
    int a[3][2] = {{0, 1}, {2, 3}, {4, 5}};
    
    ASSERT(a[0][0] == 0);
    ASSERT(a[0][1] == 1);
    ASSERT(a[1][0] == 2);
    ASSERT(a[2][1] == 5);

    int *p = a;
    
    ASSERT(p[0] == 0);
    ASSERT(p[1] == 1);
    ASSERT(p[2] == 2);
    ASSERT(p[3] == 3);
    ASSERT(p[4] == 4);
    ASSERT(p[5] == 5);
}

void test_var_indexes() {

    int a[3][2] = {{0, 1}, {2, 3}, {4, 5}};

    int i = 1;
    int j = 0;
    
    ASSERT(a[i][j] == 2)
}