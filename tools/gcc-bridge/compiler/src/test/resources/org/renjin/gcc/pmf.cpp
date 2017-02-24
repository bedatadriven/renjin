

#include <math.h>

#include "assert.h"

class Shape {
  public:
    virtual int area() = 0;
    virtual int perimeter() = 0;
};

class Rectangle : public Shape {
    int width, height;
  public:
    Rectangle(double w, double h) : width(w), height(h) {}
    virtual int area ()  { return width*height; }
    virtual int perimeter() { return width*2 + height*2; }
};

class Circle : public Shape {
    int radius;
  public:
    Circle(double r) : radius(r) { }
    virtual int area () {return radius * radius * M_PI ;}
    virtual int perimeter() {return radius * 2 * M_PI; }
};

class Distribution {
    double mean;
    double variance;
  public:
    Distribution(double m, double v) : mean(m), variance(v) {}
    double  calc_mean() {
        return mean;
    }

    double calc_variance() {
        return variance;
    }

    double calc_stddev() {
        return sqrt(variance);
    }
};


// See https://isocpp.org/wiki/faq/pointers-to-members#fnptr-vs-memfnptr-types
// For help in understanding pointer to member functions in C++...

typedef  int (Shape::*ShapeMemFn)();
typedef  double (Distribution::*DistMemFn)();


#define CALL_MEMBER_FN(object,ptrToMember)  ((object).*(ptrToMember))

void check_virtual_calc(Shape &shape, ShapeMemFn fn, double expectedValue) {
    double value = CALL_MEMBER_FN(shape, fn)();
    ASSERT(value == expectedValue);
}

void check_static_calc(Distribution &dist, DistMemFn fn, double expectedValue) {
    ASSERT(CALL_MEMBER_FN(dist, fn)() == expectedValue);
}

extern "C" void test_virtual_pmf() {
    Rectangle rect(4, 2);

    check_virtual_calc(rect, &Shape::area, 8);
    check_virtual_calc(rect, &Shape::perimeter, 4+4+2+2);
}

extern "C" void test_static_pmf() {
    Distribution normal(0, 1);

    check_static_calc(normal, &Distribution::calc_mean, 0);
    check_static_calc(normal, &Distribution::calc_variance, 1);
    check_static_calc(normal, &Distribution::calc_stddev, 1);
}