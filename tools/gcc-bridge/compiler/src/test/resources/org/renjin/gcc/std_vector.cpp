
#include <vector>
#include <algorithm>

#include "assert.h"


extern "C" void test() {

    std::vector<int> v;
    v.push_back(41);
    v.push_back(42);
       
    ASSERT(v.size() == 2);
    ASSERT(v[0] == 41)
    ASSERT(v[1] == 42)
}

extern "C" void test_sort() {

    std::vector<int> v;
    v.push_back(9);
    v.push_back(3);
    v.push_back(1);
    v.push_back(9);

    sort(v.begin(), v.end());

    ASSERT(v[0] == 1);
    ASSERT(v[1] == 3);
    ASSERT(v[2] == 9);
    ASSERT(v[2] == 9);
}
