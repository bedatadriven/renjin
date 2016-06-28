
// Test case to verify that including 
// headers that in turn include types and global variables we can't process
// don't break compilation if they're not used.

#include <iostream>
#include <fstream>


extern "C" void test_pruning() {
    // NOOP
}