
#include <stdio.h>
#define ERROR -1
#define ASSERT(x) if(!(x)) return ERROR

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

int test_offset() {
    
    int x[] = {1,2,3,4,5};
    int y[] = {3,4,5};
    
    void *xp = (x+2);
    void *yp = y;
    
    return vectors_equal(xp, yp, 3, sizeof(int));
}

int test_comparison() {
    int x[] = {1,2,3,4,5};
    int *y = x+1;
    
    ASSERT(x != y);
    ASSERT(x+1 == y);
    ASSERT(x < y);
    ASSERT(x <= y); 
    ASSERT(x+5 != y);
    return 0;
}