

#include "rect.cpp"

#include <vector>
#include <algorithm>

bool mysort(const Rectangle &x, const Rectangle &y) {
    return x.width < y.width; 
}

extern "C" void test_sort_records() {
    
    std::vector<Rectangle> v;
    v.push_back(Rectangle(9, 0));
    v.push_back(Rectangle(3, 0));    
    v.push_back(Rectangle(10, 0));
    v.push_back(Rectangle(40, 0));
    v.push_back(Rectangle(23, 0));
    v.push_back(Rectangle(99, 0));
    v.push_back(Rectangle(99, 0));
    v.push_back(Rectangle(100, 0));
            
    std::sort(v.begin(), v.end());
    
    ASSERT(v[0].width == 3)
    ASSERT(v[1].width == 9)
    ASSERT(v[2].width == 10)
}