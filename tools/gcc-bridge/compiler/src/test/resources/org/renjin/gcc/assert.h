
#ifndef ASSERT_H
#define ASSERT_H

void assertTrue(const char * desc, int x);

#define ASSERT(EXP) assertTrue(#EXP, EXP);

#endif