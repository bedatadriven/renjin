
#define SIZE 3467
#define NUM_CLUSTERS 25

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
    run_kmeans();
    run_kmeans();
    run_kmeans();
    run_kmeans();
    

}

int run_kmeans() {
  printf("starting...\n");
  double *a = malloc(sizeof(double)*SIZE * SIZE);
  int m = SIZE;
  int n = SIZE;
  double *c = malloc(sizeof(double)*SIZE * NUM_CLUSTERS);
  int k = NUM_CLUSTERS;
  int *ic1 = malloc(sizeof(int)*SIZE);
  int *ic2 = malloc(sizeof(int)*SIZE);
  int *nc = malloc(sizeof(int)*NUM_CLUSTERS);
  double *an1 = malloc(sizeof(double)*NUM_CLUSTERS); 
  double *an2 = malloc(sizeof(double)*NUM_CLUSTERS); 
  int *ncp = malloc(sizeof(int)*NUM_CLUSTERS);
  double *d = malloc(sizeof(double)*SIZE);
  int *itran = malloc(sizeof(int)*SIZE);
  int *live = malloc(sizeof(int)*SIZE);
  int iter = 10;
  double *wss = malloc(sizeof(double)*SIZE);
  int ifault;

  int ai = 0;
  // spread out points uniformly in k-space
      printf("initializing a...\n");
  int column, row;
  for(column = 0; column < m; ++column) {
      for(row = 0; row < n; ++row) {
        a[ai++] = ((double) row)/ ((double) n);
      }
    }
    // spread out the initial centers also uniformly
          printf("initializing ...\n");
    int ci = 0;
    for(column = 0; column < m; ++column) {
      for(row = 0; row < k; ++row) {
        c[ci++] = ((double) row) / ((double) k);
      }
    }
  
    
    // timing runs
    clock_t start = clock();
   
    kmns_(a, &m, &n, c, &k, ic1, ic2, nc, an1, an2, ncp, d, itran, live, &iter,
       wss, &ifault); 
       
 double elapsed = (clock() - start);
  
    printf("time (ms) = %f\n", elapsed / (CLOCKS_PER_SEC/1000));
    printf("ifault = %d, wss[0] = %f\n", ifault, wss[0]);
    
    free(a);
    free(c);
    free(ic1);
    free(ic2);
    free(nc);
    free(an1);
    free(an2);
    free(ncp);
    free(d);
    free(itran);
    free(live);
    free(wss);
}
  

