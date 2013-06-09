

#include <stdio.h>


struct account {
   int years_open;
   int plan_type;

   // not supported yet, but make sure we don't
   // loop infinitely in the mean time
   struct account * parent;
};

double account_value(struct account *pa) {
  if(pa->plan_type == 2) {
    return pa->years_open * 2500;
  } else {
    return pa->years_open * 1500;
  }
}

double test_account_value() {
  struct account ac;
  ac.years_open = 2;
  ac.plan_type = 2;
    
  return account_value(&ac);
}
