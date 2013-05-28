

#include <stdio.h>


struct account {
   int years_open;
   int plan_type;
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
