
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


/* The following is from the triangle library, which takes bit-fiddling with pointers
 * to an unconscionable extreme. 😱 */


/* An oriented triangle:  includes a pointer to a triangle and orientation.  */
/*   The orientation denotes an edge of the triangle.  Hence, there are      */
/*   three possible orientations.  By convention, each edge always points    */
/*   counterclockwise about the corresponding triangle.                      */

struct otri {
  double *tri;
  int orient;                                         /* Ranges from 0 to 2. */
};



#define decode(ptr, otri)                                                     \
  (otri).orient = (int) ((unsigned long) (ptr) & (unsigned long) 3l);         \
  (otri).tri = (double *)                                                   \
                  ((unsigned long) (ptr) ^ (unsigned long) (otri).orient)

/* encode() compresses an oriented triangle into a single pointer.  It       */
/*   relies on the assumption that all triangles are aligned to four-byte    */
/*   boundaries, so the two least significant bits of (otri).tri are zero.   */

#define encode(otri)                                                          \
  (double *) ((unsigned long) (otri).tri | (unsigned long) (otri).orient)


/* decode() converts a pointer to an oriented triangle.  The orientation is  */
/*   extracted from the two least significant bits of the pointer.           */

struct otri do_decode(double *ptr) {
    struct otri otri;
    (otri).orient = (int) ((unsigned long) (ptr) & (unsigned long) 3l);
    (otri).tri = (double *)
                      ((unsigned long) (ptr) ^ (unsigned long) (otri).orient);
    return otri;
}

double * do_encode(struct otri * potri) {
    return encode(*potri);
}


void test_bit_stashing() {

    double points[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

    struct otri a;
    a.orient = 1;
    a.tri = &points[0];

    struct otri b = do_decode(do_encode(&a));

    ASSERT(a.orient == b.orient);
    ASSERT(b.tri[0] == 1);
    ASSERT(b.tri[1] == 2);
}

