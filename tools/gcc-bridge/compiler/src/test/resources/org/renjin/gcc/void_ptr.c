

double f(double * x) {
    return *x;
}

double g(void * p) {
    return f(p);
}

double test() {
    double x = 42.0;
    return g(&x);
}
