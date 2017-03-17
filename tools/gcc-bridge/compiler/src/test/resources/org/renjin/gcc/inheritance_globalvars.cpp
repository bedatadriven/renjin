#include "assert.h"

class A {
public:
  int a;
  virtual int w() {return 0;};
};

class B {
public:
  int b;
  virtual int v() {return 1;};
};

class C : public A, public B {
public:
  int c;
  virtual int w() {return 2;};
};

int call_w(A *pa) {
  return pa->w();
}

extern "C" void test_run() {
  A a;
  C c;

  ASSERT( call_w(&a) == 0 );
  ASSERT( call_w(&c) == 2 );

}

