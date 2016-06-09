
#include <inttypes.h>
#include "assert.h"


struct st {
    int x;
    int *y;
};

void voidp_receiver(void *p) {
    struct st *pst = (struct st*)p;
    ASSERT(pst->x == 42);
}

void test_struct_cast() {
    struct st s;
    s.x = 42;
    
    voidp_receiver(&s);
}

void primitive_voidp_receiver(void *p) {
    int *pi = (int*)p;
    ASSERT(*pi == 96);
}

void test_primitive_ptr_cast() {
    int x = 96;
    primitive_voidp_receiver(&x);
}

void test_primitive_cast() {

  uint32_t endian;
  endian = 1;
  
  int little = ( (*((unsigned char *)(&endian)))  != 0);
  
  ASSERT(little == 1);

}