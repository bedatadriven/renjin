
#include <stdlib.h>
#include <stdio.h>

#include "assert.h"

typedef struct {
   double years_open;
   int plan_type;
} account;


double account_value(account * pa) {
  if(pa->plan_type == 2) {
    return pa->years_open * 2500.0;
  } else {
    return pa->years_open * 1500.0;
  }
}

void init_account(account **ppa) {
  account *pa = malloc(1 * sizeof(account));
  pa->years_open = 10;
  pa->plan_type = 1;
  *ppa = pa;
}

void test_account_value() {
  account ac;
  ac.years_open = 2;
  ac.plan_type = 2;
    
  ASSERT(account_value(&ac) == 5000);
}

void test_malloc() {
  account *pa;
  init_account(&pa);
  
  ASSERT(account_value(pa) == 15000);
}

account** do_test_pointer_pointer(int n) {
  account **pa = malloc(n * sizeof(account*));
  int i;
  for(i=0;i<n;++i) {
    pa[i] = malloc(sizeof(account));
    pa[i]->years_open = i;
    pa[i]->plan_type = 2;
  }
  return pa;
}


void do_call(account *pa, void (*fn)(void *)) {
    fn(pa);
}

void test_free_funptr() {
  account *pa;
  init_account(&pa);
  do_call(pa, &free);
}