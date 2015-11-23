

double sqr(double x) {
  return x * x;
}

double sum(double *x, int n) {
  int i;
  double s = 0;
  for(i=0; i<n;++i) {
    s += x[i];
  }
  return s;
}