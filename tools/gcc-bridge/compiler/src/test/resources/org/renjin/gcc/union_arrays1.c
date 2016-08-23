
#include "assert.h"

// Tests mixes of arrays and primitives
// within a record represented by an int[]

struct A {
    int x;
    int y;
};

struct B {
    int z[2];
};

union U1 {
    struct A a;
    struct B b;
};


void test1() {
    
    union U1 u;
    u.a.x = 41;
    u.a.y = 42;
    
    ASSERT(u.b.z[0] == 41);
    ASSERT(u.b.z[1] == 42);
}
