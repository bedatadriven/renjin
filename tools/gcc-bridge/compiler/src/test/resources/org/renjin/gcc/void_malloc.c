
#include <stdio.h>
#include <stdlib.h>

void * my_alloc(int size) {
    return malloc(size);   
}


void init_double(void *p) {
    
    double *pd = (double *) p;
    pd[0] = 3.145;
    pd[1] = 42.0;
}

double* test_double() {

    void * pv = my_alloc( sizeof(double) * 2 );

    init_double(pv);
    
    return pv;
}