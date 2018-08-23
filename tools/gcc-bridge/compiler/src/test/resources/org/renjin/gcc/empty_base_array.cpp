
#include "assert.h"

class base {

public:
    base() {}

    int do_something() { return 42; }

};

class gen : public base {

public:
    int rows;
    int cols;

    int size() { return rows*cols; }
};

// void accept_gen(IntPtr p)
void accept_gen(gen *p) {
    ASSERT(p->size() == 12)
}

// void accept_base(Object p)

void accept_base(base *p) {
    // Object p (= IntPtr)

    ASSERT(p->do_something() == 42);

    // accept_gen( (IntPtr) p )
    accept_gen((gen*)p);

    gen *gg = (gen*)p;
    ASSERT(gg->rows == 6);
    ASSERT(gg->cols == 2);

}

extern "C" void test_record() {

    gen g;
    g.rows = 6;
    g.cols = 2;

    // int[] g = new int[2];
    // int g$offset;
    // accept_base(new IntPtr(g, g$offset))
    accept_base(&g);

}

