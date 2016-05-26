
#include <math.h>

class Shape {
  public:
    virtual int area() = 0;
};

class Rectangle : public Shape {
    int width, height;
  public:
    Rectangle(double w, double h) : width(w), height(h) {}
    virtual int area ()  {return width*height;}
};


class Circle : public Shape {
    int radius;
  public:
    Circle(double r) : radius(r) { }
    virtual int area () {return radius * radius * M_PI ;}
};

int twice_area(Shape &shape) {
  return shape.area() * 2;
}

extern "C" int calc_areas () {
  Rectangle rect(3,4);
  Circle circle(9);
  
  return twice_area(rect) + twice_area(circle);
}