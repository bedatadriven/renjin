
#include "assert.h"
#include <stdlib.h>

struct _double_mat {
    /** number of rows */
    int nrow;

    /** number of columns */
    int ncol;

    /** total number of elements */
    int nelem;

    /** pointer to flat data array in row-major order */
    double *data;
};

struct _int_mat {
    /** number of rows */
    int nrow;

    /** number of columns */
    int ncol;

    /** total number of elements */
    int nelem;

    /** pointer to flat data array in row-major order */
    int *data;
};

union univ_mat {
    struct _double_mat d;
    struct _int_mat i;
};

void alloc_matrix(void **pv) {
    *pv = malloc(sizeof(double)*10);
}

int test_null_comparison() {
    
    union univ_mat b;
    
    struct _double_mat *p = &(b.d);
    
    alloc_matrix(&(p->data));
    
    if(!p->data) {
       return 1;
    } else {
        return 0;
    }
}


