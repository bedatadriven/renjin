#include "assert.h"
struct S {
    int x;
    double y;
};

void function2(struct S *x) {
    struct S **b = &x;
    struct S c;
    c.x = 45;
    c.y = 76;
    x = &c;
    ASSERT((*x).x == 45);
    ASSERT((*x).y == 76);
}

void addressed_param(struct S *s, int x, double y) {
    struct S *z = s;
    (*z).y = (double)x*y;
    ASSERT((*s).x == 41);
    ASSERT((*s).y == 17.5);
    function2(z);
}
void test_param() {
    struct S s;
    s.x = 41;
    s.y = 1.2;
    addressed_param(&s, 5, 3.5);
}