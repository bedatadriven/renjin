

#include "assert.h"

struct myt {
    int x;
    double y;
};

void triple(struct myt *ps) {
    ps->x = ps->x * 3;   
}

struct myt param_ref(struct myt s) {
    
    triple(&s);
    
    return s;
}

void test() {

    struct myt s;
    s.x = 4;
    
    struct myt t = param_ref(s);
    
    ASSERT(s.x == 4);
    ASSERT(t.x == 12);
}

void test_copy_struct() {

    struct myt a;
    struct myt b;
    
    a.x = 99;
    a.y = 13.0;
    
    memcpy(&b, &a, sizeof(struct myt));

    ASSERT(b.x == 99)
    ASSERT(a.y == 13.0)
}