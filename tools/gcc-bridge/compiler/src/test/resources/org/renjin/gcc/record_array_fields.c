
#include "assert.h"

#define MAX_METHODS 25

struct A {
    
    double x;
    double y;
    int z;
};

struct B {
    
    int p;
    float q;
    
    struct A methods[MAX_METHODS];
};

void test_init() {
    
    struct B b;
    
    ASSERT(b.methods[MAX_METHODS-1].z == 0);
}

void test_set() {
    
    struct B b1;
    struct B b2;
    
    b1.p = 42;
    b1.methods[0].x = 43;
    
    b2 = b1;
    
    b1.methods[0].x = 99;
    
    ASSERT(b2.p == 42)
    ASSERT(b2.methods[0].x == 43)
}
