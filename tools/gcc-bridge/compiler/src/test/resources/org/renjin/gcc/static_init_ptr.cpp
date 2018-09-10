
#include <stdio.h>
#include "assert.h"

static int* INITIAL_STATE = new int(13);

extern "C" void test_foobar_initial_state() {
    ASSERT(*INITIAL_STATE == 13);
}


extern "C" void test_bar_initial_state() {
    ASSERT(*INITIAL_STATE == 13);
}

int main() {
    test_foobar_initial_state();
    test_bar_initial_state();

    return 0;
}