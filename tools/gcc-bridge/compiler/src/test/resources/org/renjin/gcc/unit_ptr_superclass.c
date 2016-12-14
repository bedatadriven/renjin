
#include <stdlib.h>
#include "assert.h"

struct Parent {
    double a;
    int b;
};

struct Child {
    struct Parent parent;
    double c;
    int d;
    int e;
};


void test_unit_pointer() {

    struct Child *pchildren = malloc(sizeof(struct Child)*50);

    pchildren[11].parent.a = 1.5;
    pchildren[11].parent.b = 99;
    pchildren[11].c = 41;
    pchildren[11].d = 42;
    pchildren[11].e = 43;

    struct Child *p11 = pchildren+11;

    parent_tester(p11);
}

void parent_tester(struct Parent *pp) {
    ASSERT(pp->a == 1.5);
    ASSERT(pp->b == 99);
}
