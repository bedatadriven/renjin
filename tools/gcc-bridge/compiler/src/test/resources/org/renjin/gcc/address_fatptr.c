#include "assert.h"

void g(double** pp){
  ASSERT(**pp == 10);
}

void f(double* p){
  double* v;
  v = p;
  g(&p);

  ASSERT(*p == 10);
  ASSERT(*v == 10);
}

void test_fatptr(){
  double x = 10;
  f(&x);
}