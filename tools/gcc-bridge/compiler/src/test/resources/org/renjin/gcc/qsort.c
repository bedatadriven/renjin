
#include <stdio.h>
#include <stdlib.h>

#include "assert.h"

int values[] = { 88, 56, 100, 2, 25 };

struct A {
    int x;
    double y;
};

int cmpfunc (const void * a, const void * b)
{
   return ( *(int*)a - *(int*)b );
}

int rcmpfunc (const void * a, const void * b)
{
   struct A *pa = (struct A*)a;
   struct A *pb = (struct A*)b;

   return ( pa->x - pb->x);
}

/*
void test_qsort_primitives()
{
//   qsort(values, 5, sizeof(int), cmpfunc);

// 2 25 56 88 100
   ASSERT(values[0] == 2)
   ASSERT(values[1] == 25)
   ASSERT(values[2] == 56)
   ASSERT(values[3] == 88)
   ASSERT(values[4] == 100)
}
*/

void test_qsort_records()
{
    struct A *a = malloc(sizeof(struct A)*2);
    a[0].x = 2;
    a[0].y = 2.5;

    a[1].x = 1;
    a[1].y = 1.5;

    qsort(a, 2, sizeof(struct A), rcmpfunc);

    ASSERT(a[0].x == 1);
    ASSERT(a[0].y == 1.5);

    ASSERT(a[1].x == 2);
    ASSERT(a[1].y == 2.5);
}