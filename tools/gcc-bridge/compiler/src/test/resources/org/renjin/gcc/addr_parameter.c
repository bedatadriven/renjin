#include "assert.h"

struct SS {
    int a;
    double b;
    char c;
};

struct S {
    int x;
    double y;
    struct SS z;
};

void addressed_param(struct S *ps) {

    ASSERT(ps->x == 41);
    ASSERT(ps->z.a == 37);

    struct S **pps = &ps;
    ASSERT((*pps)->x == 41);

    struct S *ps2 = ps;
    struct S s;
    ps = &s;
    ps->x = 42;

    ASSERT((*pps)->x == 42);
    ASSERT(ps2->x == 41);
}

void test_param() {
    struct S s;
    s.x = 41;
    s.y = 1.2;
    s.z.a = 37;
    addressed_param(&s);
}

void test_param2() {
    struct S s;
    s.x = 41;
    s.y = 1.2;
    s.z.a = 37;
    struct S s2;
    s2 = s;
    s2.z.a = 27;
    ASSERT(s.z.a == 37);
}

