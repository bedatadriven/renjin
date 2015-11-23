
#include <stdlib.h>
#include <stdio.h>


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

double test_account_value() {
  account ac;
  ac.years_open = 2;
  ac.plan_type = 2;
    
  return account_value(&ac);
}

double test_malloc() {
  account *pa;
  init_account(&pa);
  return account_value(pa);
}


