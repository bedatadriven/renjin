

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

double test_account_value() {
  account ac;
  ac.years_open = 2;
  ac.plan_type = 2;
    
  return account_value(&ac);
}

double test_malloc() {
  account *ac = malloc(1 * sizeof(account));
  ac->years_open = 2;
  ac->plan_type = 2;
  return account_value(ac);
}