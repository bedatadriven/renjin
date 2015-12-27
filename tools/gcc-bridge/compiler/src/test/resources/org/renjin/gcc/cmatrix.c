
#include <stdlib.h>

// Creates a "jagged" matrix,
static double **cmatrix(double *data, int nrow, int ncol) {

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

static double* get_at(double **matrix, int row, int col) {
  return &matrix[row][col];
}

static double sum_second_col(double *data, int nrow, int ncol) {
  double ** matrix = cmatrix(data, nrow, ncol);
  
  int i;
  double sum = 0;
  for(i=0;i<nrow;++i) {
    sum += matrix[i][1];
  }
  
  return sum;
}
