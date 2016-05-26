
#include "assert.h"

class Rectangle {
  public:
    int width, height;
    double stroke_width;
    void set_values (int,int);
    int area () {return width*height;}
} rect;


void Rectangle::set_values (int x, int y) {
  width = x;
  height = y;
}

Rectangle& embiggen(Rectangle& r) {
  r.width = r.width * 10;
  r.height = r.height * 10;
  return r;
}

extern "C" int test_calc_area () {
  Rectangle rect;
  rect.set_values (3,4);
  ASSERT(rect.area() == 12);
}

extern "C" int test_references() {
 Rectangle rect;
 rect.set_values (3,4); 
 
 // Assignment from ref to value. 
 Rectangle other = embiggen(rect);
 
 ASSERT(other.area() == 1200)
}