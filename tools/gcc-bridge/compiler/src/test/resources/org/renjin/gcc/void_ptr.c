

double f(double * x) {
    return *x;
}

double g(void * p) {
    return f(p);
}

double pass_through() {
    double x = 42.0;
    return g(&x);
}
