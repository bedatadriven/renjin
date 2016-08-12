
#include <stdio.h>
#include "assert.h"

// From nmath/wilcox.c

double *** init(int m, int n) {
    int i;
    double ***w;

	w = (double ***) calloc((size_t) m, sizeof(double **));
    w[0] = (double **) calloc((size_t) n, sizeof(double *));
	return w;
}

void test_init() {
    
    double ***w = init(5, 4);
    
    ASSERT(w[0][2] == 0);
}