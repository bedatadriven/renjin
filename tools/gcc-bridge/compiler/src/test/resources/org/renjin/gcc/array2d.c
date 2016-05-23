

#include "assert.h"

double srgb_xyz[][3] = {
  {0.416821341885317054, 0.35657671707797467, 0.179807653586085414},
  {0.214923504409616606, 0.71315343415594934, 0.071923061434434166},
  {0.019538500400874251, 0.11885890569265833, 0.946986975553383292}
};


void test() {
    int a[3][2] = {{0, 1}, {2, 3}, {4, 5}};
    
    ASSERT(a[0][0] == 0);
    ASSERT(a[0][1] == 1);
    ASSERT(a[1][0] == 2);
    ASSERT(a[2][1] == 5);

    int *p = a;
    
    ASSERT(p[0] == 0);
    ASSERT(p[1] == 1);
    ASSERT(p[2] == 2);
    ASSERT(p[3] == 3);
    ASSERT(p[4] == 4);
    ASSERT(p[5] == 5);
}

void test_var_indexes() {

    int a[3][2] = {{0, 1}, {2, 3}, {4, 5}};

    int i = 1;
    int j = 0;
    
    ASSERT(a[i][j] == 2)
}

void test_global_init() {

    ASSERT(srgb_xyz[2][3] == 0.946986975553383292);
    
}