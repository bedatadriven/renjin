
#include <stdio.h>
#include <string.h>

#include "assert.h"

struct A {
    double x;
    double y;
    int qa[3];
    int q;
};


void init_field(double *p) {
    *p = 42;
}

void test_memset_addressable_field() {
    struct A a;
    init_field(&a.x);
    
    ASSERT(a.x == 42);
    
    memset(&a, 0, sizeof(struct A));
    
    ASSERT(a.x == 0);
}

void test_partial_array_memset() {
    struct A a;
    a.x = 31;
    a.y = 32;
    a.qa[0] = 41;
    a.qa[1] = 42;
    a.qa[2] = 43;
    a.q = 44;
    
    memset(&a, 0, sizeof(double)*2 + sizeof(int)*2);
    
    ASSERT(a.x == 0);
    ASSERT(a.y == 0);
    ASSERT(a.qa[0] == 0);
    ASSERT(a.qa[1] == 0);
    
    // the rest of the struct should *not* have been affected
    ASSERT(a.qa[2] == 43);
    ASSERT(a.q == 44);

}