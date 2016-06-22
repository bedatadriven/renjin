

// Test case extracted from bug in survival/src/coxfit6.c

double* loglik_test() {
  double *loglik = malloc(2 * sizeof(double));
  loglik[1] = 34;
  loglik[0] = loglik[1];
  return loglik;
}