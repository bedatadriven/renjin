
#include "assert.h"

struct A {
    int i;
    double d;
};

struct B {
    int i;
    float f;
};

struct C {
    struct A a;
    struct B b;
    int i;
};

void test_superclass() {

    struct C c;
    c.i = 92;
    c.a.i = 41;
    c.a.d = 42.5;
    c.b.i = 21;
    c.b.f = 1.5;

    struct A *pa1 = (struct A *)&c;
    struct A *pa2 = &(c.a);

    ASSERT(pa1 == pa2);
    ASSERT(pa1->i == 41);
    ASSERT(pa1->d == 42.5);

    struct B *pb = &c.b;
    ASSERT(pb->i == 21);
    ASSERT(pb->f == 1.5);

    ASSERT(c.i == 92);
}
