
#include "assert.h"

struct C {
    double m;
    int n[3];
};

struct D {
    double p;
    int q[2];
    int r;
};

union U2 {
    struct C c;
    struct D d;
};


void test2() {
    
    union U2 u;
    u.c.m = 1.5;
    u.c.n[0] = 91;
    u.c.n[1] = 92;
    u.c.n[2] = 93;
    
    ASSERT(u.d.p == 1.5);
    ASSERT(u.d.q[0] == 91);
    ASSERT(u.d.q[1] == 92);
    ASSERT(u.d.r == 93);

    u.d.r = 44;
    
    ASSERT(u.c.n[2] == 44);
}
