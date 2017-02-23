
#include "assert.h"

#include <stdlib.h>

struct data_header {
    int value;
    void **parent_headers;
};


void test_derefence() {

    struct data_header a;
    a.value = 41;

    struct data_header b;
    b.value = 42;

    struct data_header parent;
    parent.value = 40;
    parent.parent_headers = malloc(sizeof(void*) * 2);
    parent.parent_headers[0] = &a;
    parent.parent_headers[1] = &b;

    deference(&parent);
}

void deference(struct data_header *pparent) {

    struct data_header *pa = (struct data_header*)pparent->parent_headers[0];
    ASSERT(pa->value == 41);
}

