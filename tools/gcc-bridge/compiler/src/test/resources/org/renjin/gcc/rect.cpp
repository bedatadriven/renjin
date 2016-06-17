
#include "assert.h"

class Rectangle {
  public:
    int width, height;
    double stroke_width; 
    void set_values (int,int);
    
    Rectangle() {
        width = 0;
        height = 0;
    }
    
    Rectangle( const Rectangle& other){
        width = other.width;
        height = other.height;
    }

    
    int area () {return width*height;}
    
    Rectangle copy()  {
        Rectangle r;
        r.set_values(width, height);
        return r;
    }
    
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


extern "C" Rectangle test_copy_constructor() {
  Rectangle rect;
  rect.set_values (3,4);
  
  Rectangle newRect = rect.copy();
  
  ASSERT(newRect.area() == 12);
}


extern "C" int test_references() {
 Rectangle rect;
 rect.set_values (3,4); 
 
 // Assignment from ref to value. 
 Rectangle other = embiggen(rect);
 
 ASSERT(other.area() == 1200)
}

int compare_refs(Rectangle *a1, Rectangle *a2, Rectangle *b) {
  ASSERT(a1 == a1);
  ASSERT(a1 == a2);
  ASSERT(a1 != b);
  ASSERT( (a1 < b) || (a1 > b));    
}

int compare_casted_ints(int a1, int a2, int b) {
  ASSERT(a1 == a1);
  ASSERT(a1 == a2);
  ASSERT(a1 != b);
  ASSERT( (a1 < b) || (a1 > b));   
}

extern "C" int test_refEquality() {
  Rectangle a;
  Rectangle b;
  
  compare_refs(&a, &a, &b);
  compare_casted_ints((int)&a, (int)&a, (int)&b);
}