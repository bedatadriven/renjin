
#include "assert.h"

class base {

public:
    base() {}

    int do_something() { return 42; }

};

class gen : public base {

public:
    int rows;
    double cols;

    int size() { return rows*cols; }
};

void accept_gen(gen *p) {
    ASSERT(p[2].rows == 13);
    ASSERT(p[2].cols == 2);
}

void accept_base(base *p) {
    // Object p (= IntPtr)

    ASSERT(p->do_something() == 42);

    accept_gen((gen*)p);

    gen *gg = (gen*)p;
    ASSERT(gg[2].rows == 13);
    ASSERT(gg[2].cols == 2);
}

extern "C" void test_record() {

    gen g[3];    // java: gen g[] = new gen[3];
    g[0].rows = 6;
    g[0].cols = 2;
    g[1].rows = 3;
    g[1].cols = 9;
    g[2].rows = 13;
    g[2].cols = 2;

    accept_base(g); // java: accept_base(new ObjectPtr(g, 0))

}

