
#include <stdlib.h>

#include "assert.h"

static struct {
        int buflength;
        char *seq;
        int seqlength;
        int LCP;
        int j0, shift0;
        int *VSGSshift_table;
        int *MWshift_table;
} ppP = {0, NULL, 0, -1, 0, 0, NULL, NULL};

void test_constructor() {

    ASSERT(ppP.buflength == 0);
    ASSERT(ppP.LCP == -1);
}