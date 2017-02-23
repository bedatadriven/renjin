

#include "assert.h"

class my_empty_class {

public:
    my_empty_class() {}

    int do_something() { return 42; }

};



void pass_by_value(my_empty_class v, my_empty_class &p) {
    ASSERT(v.do_something() == 42);
    ASSERT(&v != &p);
}


extern "C" void test_empty_copy() {

    my_empty_class a;
    pass_by_value(a, a);
}