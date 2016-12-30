
#include <stdlib.h>
#include "assert.h"

struct S {
    double x;
    int count;
};

struct U {
    double x;
    double y;
    int count;
};

void* do_alloc(int n) {
    return malloc(sizeof(struct S) * n);
}

int receive_ptr(struct S *s) {
    return s[0].count + s[1].count;
}

struct S * dummy() {
    // This is only declared to force S to be treated as non-unit pointer
    struct S * ps = malloc(sizeof(struct S)*10);
    return ps;
}

void test_param() {

    void * p = do_alloc(4);
    int total_count = receive_ptr(p);

    ASSERT(total_count == 0);
}

struct U * init_u(struct U *pu) {
    pu->x = 1.5;
    pu->y = 2.3;
    pu->count = 44;
    return pu;
}

void test_unit_param() {
    void *pv = malloc(sizeof(struct U));

    struct U *pu = init_u(pv);

    ASSERT(pu->x == 1.5);
    ASSERT(pu->y == 2.3);
    ASSERT(pu->count == 44);
}