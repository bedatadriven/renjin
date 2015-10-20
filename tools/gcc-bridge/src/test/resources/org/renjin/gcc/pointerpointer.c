

void init_array(double **x) {
  double *y = malloc(sizeof(double)*10);
  int i;
  for(i=0;i<10;++i) {
    y[i] = i;
  }
  *x = y;
}

double sum_array(double* values, int length) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += values[i];
  }
  return sum;
}


double test() {
  double *x = 0;
  init_array(&x);
  return sum_array(x, 10);
}
