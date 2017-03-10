
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
    ASSERT(p->rows == 6);
    ASSERT(p->cols == 2);
}

void accept_base(base *p) {
    // Object p (= IntPtr)

    ASSERT(p->do_something() == 42);

    // accept_gen( (ObjectPtr) p )
    accept_gen((gen*)p);

    gen *gg = (gen*)p;
    ASSERT(gg->rows == 6);
    ASSERT(gg->cols == 2);
}

extern "C" void test_record() {

    gen g;    // java: gen g[] = new gen[3];
    g.rows = 6;
    g.cols = 2;

    accept_base(&g); // java: accept_base(new ObjectPtr(g, 0))

}

