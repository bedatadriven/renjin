
#ifndef ASSERT_H
#define ASSERT_H

#ifdef __cplusplus
extern "C" {
#endif

void assertTrue(const char * desc, int x);


#ifdef __cplusplus
}
#endif

#define ASSERT(EXP) assertTrue(#EXP, EXP);


#endif