
#include <stdlib.h>

// Creates a "jagged" matrix,
static double **cmatrix(double *data, int ncol, int nrow) {

  int i,j;
  double **pointer;
  double *temp;

  pointer = malloc(nrow * sizeof(double *));
  temp = malloc(nrow*ncol * sizeof(double));
  if (data==0) {
    for (i=0; i<nrow; i++) {
        pointer[i] = temp;
        temp += ncol;
    }
  } else {
    for (i=0; i<nrow; i++) {
      pointer[i] = temp;
      for (j=0; j<ncol; j++) *temp++ = *data++;
    }
  }
  return(pointer);
}


