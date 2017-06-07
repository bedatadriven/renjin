

#include "assert.h"

struct S {
    int f1;
    double f2;
};

struct A {
    double x;
    struct S s[1];
};

struct B {
    double x;
    struct S s[2];
};

struct C {
    double x;
    struct S s[3];
};

union U {
    struct A a;
    struct B b;
    struct C c;
};


void test_union() {

    union U u;

    u.a.x = 43.5;

    ASSERT(u.a.x == 43.5);
    ASSERT(u.b.x == 43.5);
    ASSERT(u.c.x == 43.5);

    u.c.s[0].f1 = 100;
    u.c.s[1].f1 = 102;
    u.c.s[2].f1 = 103;

    ASSERT(u.a.s[0].f1 == 100);

}