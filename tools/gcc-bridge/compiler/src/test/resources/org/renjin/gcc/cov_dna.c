
// Test case from package etm
// https://github.com/cran/etm/blob/0.6-2/src/cov_dna.c

#include <stdio.h>
#include <math.h>

void cov_dna(double *nrisk, double *nev, int *dd, double *cov) {
    
    const int d = *dd;
    const int D = pow(d, 2);
    double temp_cov[D][D];
    double t_cov[D*D];
    double sum_nev[d];
    int a, b, i, j, k, l, e, f;
    double nr = 0;
    double temp[d][d];

   
    /* Initialisation */
    for (a = 0; a < d; ++a) {
	sum_nev[a] = 0;
	for (b = 0; b < d; ++b) {
	    temp[a][b] = 0.0;
	}
    }
    for (a = 0; a < D; ++a) {
	for (b = 0; b < D; ++b) {
	    temp_cov[a][b] = 0.0;
	    t_cov[a + D*b] = 0.0;
	}
    }
    

    for (a = 0; a < d; ++a) {
	for (b = 0; b < d; ++b) {
	    sum_nev[a] += nev[a + d * b];
	}
    }
    /******************/

    /* loops on the blocks */
    for (i = 0; i < d; ++i) {
	for (j = 0; j < d; ++j) {
	    		
	    /* loops in the blocks */
	    for (k = 0; k < d; ++k) {
		for (l = 0; l < d; ++l) {
		    if (nrisk[k] != 0) {
			nr = pow(nrisk[k], 3);
			if (k == l) {
				if (k == i) {
				    if (l == j) {
					temp[k][l] = ((nrisk[k] - sum_nev[k]) * sum_nev[k]) / nr;
				    }
				    else {
					temp[k][l] = -(((nrisk[k] - sum_nev[k]) * nev[k + j * d]) / nr);
				    }
				}
				else {
				    if (i != k && j != k) {
					if (i == j) {
					    temp[k][l] = ((nrisk[k] - nev[k + i*d]) * nev[k + i*d])/ nr;
					}
					else {
					    temp[k][l] = (-nev[k + i*d] * nev[k + j*d]) / nr;
					}
				    }
				}
			    }
		    }

		    temp_cov[i * d + k][j * d + l] = temp[k][l];
		    for (e = 0; e < d; ++e) {
			for (f = 0; f < d; ++f) {
			    temp[e][f] = 0.0;
			}
		    }
		}
	    }
	}
    }
    
    for (i = 0; i < D; ++i) {
	for (j = 0; j < D; ++j) {
	    t_cov[i + j*D] = temp_cov[i][j];
	}
    }
	
    for (i = 0; i < D; ++i) {
	for (j = 0; j < D; ++j) {
	    if (t_cov[j * D + i] != 0.0) {
		cov[j * D + i] = t_cov[j * D + i];
		cov[i * D + j] = t_cov[j * D + i];
		t_cov[j * D + i] = cov[j * D + i];
	    }
	}
    }
}	