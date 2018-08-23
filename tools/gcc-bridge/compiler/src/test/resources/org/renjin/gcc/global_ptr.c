
#include "assert.h"

static int global_value;
static int second_value;
static int* global_ptr;


static init_pointer() {
    global_value = 99;
    global_ptr = &global_value;
}

static reinit_pointer(int** ppint) {
    *ppint = &second_value;
}

void test_addressable_pointers() {
    init_pointer();
    ASSERT(*global_ptr == 99);

    reinit_pointer(&global_ptr);
    second_value = 41;
    ASSERT(*global_ptr == 41);
}