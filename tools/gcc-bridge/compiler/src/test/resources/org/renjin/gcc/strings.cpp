
#include <string>

#include "assert.h"

using namespace std;

// This does not run because we are missing compiled
// functions from the c++ standard library (most things are
// from headers, but not some things for basic_string
extern "C" void do_not_test_string() {
    
    string x("hello world");
    string y("hello");
    y += " world";
    
    ASSERT(x == y);
}

void check_value_type(int value_type_size) {
    ASSERT(value_type_size == sizeof(char))
}

extern "C" void test() {
    check_value_type(sizeof(string::value_type));
}