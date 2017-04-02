
#include <stdio.h>
#include <stdlib.h>
#include "assert.h"


typedef double(*func_ptr_t)(double);


struct B {
    func_ptr_t * vtbl;
    int offset;
};

struct A {
    struct B b;
    int count;
    double *pd;
};


double times2(double x) {
    return x * 2;
}

void do_cast(struct A * pa) {
    func_ptr_t *vtbl = *( (func_ptr_t**)pa );
    func_ptr_t f = *vtbl;

    ASSERT(f(41) == 82.0);
}

void test_first_field_cast() {

    struct A a[2];
    a[0].b.vtbl = malloc(sizeof(func_ptr_t)*2);
    a[0].b.vtbl[0] = &times2;

    do_cast(&a[0]);
}
