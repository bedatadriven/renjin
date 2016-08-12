
#include <stdlib.h>

#include "assert.h"

struct a_t {
    int x;
    double y;
};

void do_print_pointer(struct a_t *p) {
    printf("p = %p", p);
}

void test_print_wrapped_ptr() {
    struct a_t a[3];
    do_print_pointer(a);
}