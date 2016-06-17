
#include <vector>
#include "assert.h"

void test() {

    std::vector<int> v;
    v.push_back(41);
    v.push_back(42);
       
    ASSERT(v.size() == 2);
    ASSERT(v[0] == 41)
    ASSERT(v[1] == 42)
}