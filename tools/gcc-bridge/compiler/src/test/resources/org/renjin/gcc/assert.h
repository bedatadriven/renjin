
#ifndef ASSERT_H
#define ASSERT_H

#include <stdlib.h>
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef _RENJIN
void assertTrue(const char * desc, int x);
#else
void assertTrue(const char * desc, int x) {
    if(x==0) {
        printf("ASSERTION FAILED: %s\n", desc);
        exit(-1);
    }
}
#endif

#ifdef __cplusplus
}
#endif

#define ASSERT(EXP) assertTrue(#EXP, EXP);


#endif