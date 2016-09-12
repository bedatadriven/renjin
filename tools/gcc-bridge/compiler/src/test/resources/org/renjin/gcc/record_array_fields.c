
#include <stdlib.h>

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

struct P {
    int x;
    int y;
};

struct C {
    struct P points[5];
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

void test_record_array_set() {
    struct C c1;
    struct C c2;
     
    c1.points[0].x = 41;
    c1.points[0].y = 42;
    c1.points[4].x = 30;
    c1.points[4].y = 31;
    
    c2 = c1;
    
    ASSERT(c2.points[4].x == 30)
    ASSERT(c2.points[4].y == 31)
    
    memset(&c2, 0, sizeof(struct C));
    
    // c2 is zero-ed out
    ASSERT(c2.points[4].x == 0)
    ASSERT(c2.points[4].y == 0)
    
    // c1 remains untouched
    ASSERT(c1.points[4].x == 30)
    ASSERT(c1.points[4].y == 31)

}