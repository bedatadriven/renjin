
#include "assert.h"

struct a_t {
    double x;
    int y;
};

struct b_t {
    struct a_t a;
    long z;
};

void test_pointers() {
    struct b_t b;
    b.a.x = 1;
    b.a.y = 2;
    b.z = 3;
    
    struct a_t *pa = (struct a_t*)&b;
    
    ASSERT(pa->x == 1);
    ASSERT(pa->y == 2);
}

void test_set() {
    struct b_t b1;
    b1.a.x = 1;
    b1.a.y = 2;
    b1.z = 3;
    
    struct b_t b2;
    b2 = b1;
    
    b1.a.x = 99;
    
    ASSERT(b2.a.x == 1);
    ASSERT(b2.a.y == 2);
    ASSERT(b2.z == 3);
}

void test_super_set() {

    struct b_t b;
    b.a.x = 1;
    b.a.y = 2;
    b.z = 3;
    
    struct a_t a;
    a.x = 41;
    a.y = 42;
    
    b.a = a;
    
    ASSERT(b.a.x == 41);
    ASSERT(b.a.y == 42);
}