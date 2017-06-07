
#include "assert.h"

#define NULL 0

typedef struct __MethodDef {
    const char *name;
    int *types;

} R_CMethodDef;



static const R_CMethodDef cMethods[] = {
    {"C_mvtdst", (int[13]){10, 11, 12, 13, 14 } },  // array size doesn't match literal...
    {NULL, NULL}
};


void test_init() {
    ASSERT(cMethods[0].types[1] == 11);
    ASSERT(cMethods[0].types[2] == 12);

    // Size of the array is determined by the type declaration,
    // not the length of the array literal!
    ASSERT(cMethods[0].types[12] == 0);

}
