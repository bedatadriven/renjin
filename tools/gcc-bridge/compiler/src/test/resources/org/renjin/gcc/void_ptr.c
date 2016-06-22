

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

void * new_double() {
    double *p = (double*)malloc(10 * sizeof(double));
    p[0] = 41;
    p[1] = 42;
    p[2] = 43;
    
    return p;
}

double test_from_void() {
    double * x = new_double();
    
    return (x[2] == 43);
}