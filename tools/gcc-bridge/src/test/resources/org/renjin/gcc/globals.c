
#include <math.h>

// for the present time, globals need
// to be defined again in the java code

static const int my_global = 42;

static int my_mutable_global;

static int my_addressable_global;

static int magic_number() {
    return my_global;
}

void init_primitive() {
    my_mutable_global = 99;
}

//void update_pointer(int *x) {
//    *x = 144;
//}
//
//int test_addressable() {
//    update_pointer(&my_addressable_global);
//}