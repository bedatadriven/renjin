
#include "assert.h"

struct A {
    int x;
    int c[5];
    double y;
};

void test_field_offset_ref() {

    struct A a;
    a.c[3] = 300;
    a.c[4] = 400;

    int b[5];

    memcpy(&b, &a.c, sizeof(int) * 5);

    ASSERT(b[3] == 300);
    ASSERT(b[4] == 400);
}