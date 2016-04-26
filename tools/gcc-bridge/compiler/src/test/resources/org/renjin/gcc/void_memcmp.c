
#include <stdio.h>

int vectors_equal(void *x, void *y, int elements, int elementSize) {
    return memcmp(x, y, elements * elementSize) == 0; 
}

int test_double() {
    
    double x[] = {1.5,2.0,3.9};
    double y[] = {1.5,2.0,3.9};
    
    return vectors_equal(x, y, 3, sizeof(double));
}

int test_integer() {
    
    int x[] = {1,2,3,4,5};
    int y[] = {1,3,2,4,6};
    
    return vectors_equal(x, y, 6, sizeof(int));
}


