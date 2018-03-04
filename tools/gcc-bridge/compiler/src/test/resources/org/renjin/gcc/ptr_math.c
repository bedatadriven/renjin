
#include "assert.h"


char * next_aligned(char * p, unsigned int alignbytes) {


  unsigned long alignptr;

  /* Find the first item in the pool.  Increment by the size of (VOID *). */
  alignptr = (unsigned long) (p + 1);
  /* Align the item on an `alignbytes'-byte boundary. */

  return (char *)
    (alignptr + (unsigned long) alignbytes -
     (alignptr % (unsigned long) alignbytes));

}



void test_pointer_math() {

    char * p = "abcdefghijklmnopqrstuvwxyz";

    ASSERT(*next_aligned(p, 4) == 'e');
    ASSERT(*next_aligned(p+1, 4) == 'e');
}