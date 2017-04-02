
#include "assert.h"

struct S {
    int x;
    double y;
};


void receiver(struct S s) {
    ASSERT(s.x == 41);
    ASSERT(s.y == 42);
}

void receiver_with_addressing(struct S s) {
    struct S *ps = &s;
    receiver(*ps);
}

void test_param() {

    struct S *s = malloc(sizeof(struct S) * 10);
    s[4].x = 41;
    s[4].y = 42;

    receiver(s[4]);
    receiver_with_addressing(s[4]);
}