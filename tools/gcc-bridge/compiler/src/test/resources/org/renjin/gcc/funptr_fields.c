
struct A {
    int n;
    double (*fd)(double);
};

struct B {
    int n;
    int (*fi)(int);
};

union U {
    struct A a;
    struct B b;
};

double cube(double x) {
    return x * x * x;
}

void compute(union U *u, double *x) {
    int i;
    for(i=0;i<3;i++) {
        x[i] = u->a.fd(x[i]);
    }
}

void test() {

    union U u;
    u.a.fd = &cube;

    double x[] = {41, 42, 43};
    
    compute(&u, x);
}
