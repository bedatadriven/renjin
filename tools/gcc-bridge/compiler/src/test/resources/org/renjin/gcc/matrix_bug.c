#include <stdlib.h>
#include <inttypes.h>
#include "assert.h"

void loop(int *i, int len_i, int nnz_val) {

    int ii;
    int j_val;
    int64_t len_val = (int64_t)len_i;
    int64_t ii_val;// == "running" index (i + jj*len_i) % len_val for value[]
    for(ii = 0; ii < len_i; ii++, ii_val++) {

        int i__ = i[ii], px1, p2;

        if(nnz_val && ii_val >= len_val) { // "recycle" indexing into value[]
          //  ii_val -= len_val; // = (ii + jj*len_i) % len_val
            j_val = 0;
        }

        j_val++;
  }
}