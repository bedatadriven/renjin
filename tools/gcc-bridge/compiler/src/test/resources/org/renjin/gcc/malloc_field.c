
#include <stddef.h>

#include "assert.h"

struct A {
    int x;
    double y;
    void *obj;
};


struct B {
    int n;
    double z;
};

void do_malloc(struct A *pa, size_t size) {

    pa->obj = malloc(size);
}

void test_malloc_void_field() {

    struct A a;
    struct B *pb;

    do_malloc(&a, sizeof(struct B));

    pb = a.obj;

    ASSERT(pb->n == 0);
}