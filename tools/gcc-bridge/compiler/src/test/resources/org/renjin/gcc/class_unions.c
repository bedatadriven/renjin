
#include "assert.h"

struct A {
    int nrow;
    int ncol;
    int nelem;
    
    int* data;
};

struct B {
    int nrow;
    int ncol;
    int nelem;
    
    double *data;
};

union C {
    struct A a;
    struct B b;
};

void test_simple_fields() {
    union C c;
    struct A *a = &c.a;
    struct B *b = &c.b;
    
    a->nrow = 14;
    a->ncol = 3;
    
    ASSERT(b->nrow == 14)
    ASSERT(b->ncol == 3)
}

void test_unioned_pointers() {
    union C c;
    struct A *a = &c.a;
    struct B *b = &c.b;
    
    int data[3] = {91, 92, 93};
    c.a.data = &data[0];
    
    ASSERT(a->data[0] == 91)
    ASSERT(a->data[1] == 92)
    ASSERT(a->data[2] == 93);
}
