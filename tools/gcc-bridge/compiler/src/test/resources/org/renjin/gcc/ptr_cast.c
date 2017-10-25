
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>

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

void test_primitive_arith() {

  int a[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
  
  int *pstart = &a[0];
  int *pend = &a[5];
  
  int start = (int)pstart;
  int end = (int)pend;
  
  int count = (end - start) / sizeof(int);

  ASSERT(count == 5);

}

void test_int16() {
    int16_t shorts[4];
    shorts[0] = -325;
    shorts[1] = 32767;
    shorts[2] = -32768;
    shorts[3] = 128;

    // Test reading bytes...
    uint8_t *pb = &shorts[0];
    ASSERT(pb[0] == 0xbb);
    ASSERT(pb[1] == 0xfe);
    ASSERT(pb[2] == 0xff);
    ASSERT(pb[3] == 0x7f);
    ASSERT(pb[4] == 0x00);
    ASSERT(pb[5] == 0x80);
    ASSERT(pb[6] == 0x80);
    ASSERT(pb[7] == 0x00);

    // Write a few bytes
    pb[0] = 0xCC;
    pb[3] = 0x00;

    ASSERT(pb[0] == 204);
    ASSERT(pb[1] == 254);

    // Now shorts from bytes..
    uint8_t bytes[8] = { 0xbb, 0xfe, 0xff, 0x7f, 0x00, 0x80, 0x80, 0x00 };
    int16_t *ps = &bytes[0];

    ASSERT(ps[0] == -325);
    ASSERT(ps[1] == 32767);
    ASSERT(ps[2] == -32768);
    ASSERT(ps[3] == 128);

    // Now update a shorts
    ps[0] = 204;
    ps[1] = -254;
    ps[1] = -254;

    ASSERT(bytes[0] == 0xCC);
    ASSERT(bytes[1] == 0x00);
    ASSERT(bytes[2] == 0x02);
    ASSERT(bytes[3] == 0xff);
}

void test_uint16() {
    int16_t chars[4];
    chars[0] = 325;
    chars[1] = 32767;
    chars[2] = 0xFFFF;
    chars[3] = 0x1000;

    // Test reading bytes...
    uint8_t *pb = &chars[0];

    int i;
    for(i=0;i<8;++i) {
        printf("pb[%d] = %x\n", i, pb[i]);
    }

    ASSERT(pb[0] == 0x45);
    ASSERT(pb[1] == 0x01);
    ASSERT(pb[2] == 0xff);
    ASSERT(pb[3] == 0x7f);
    ASSERT(pb[4] == 0xff);
    ASSERT(pb[5] == 0xff);
    ASSERT(pb[6] == 0x00);
    ASSERT(pb[7] == 0x10);

    // Write a few bytes
    pb[0] = 0xCC;
    pb[3] = 0x00;

    ASSERT(chars[0] == 0x01CC);
    ASSERT(chars[1] == 0x00FF);


    // Now chars from bytes..
    uint8_t bytes[8] = { 0xbb, 0xfe, 0xff, 0x7f, 0x00, 0x80, 0x80, 0x00 };
    uint16_t *pc = &bytes[0];

    for(i=0;i<4;++i) {
        printf("pc[%d] = %x\n", i, pc[i]);
    }

    ASSERT(pc[0] == 0xfebb);
    ASSERT(pc[1] == 0x7fff);
    ASSERT(pc[2] == 0x8000);
    ASSERT(pc[3] == 0x0080);

    // Now update a few chars
    pc[0] = 0xCAFE;
    pc[1] = 0xD0EB;

    printf("b[0] == %x\n", bytes[0]);
    printf("b[2] == %x\n", bytes[1]);
    printf("b[2] == %x\n", bytes[2]);
    printf("b[3] == %x\n", bytes[3]);

    ASSERT(bytes[0] == 0xFE);
    ASSERT(bytes[1] == 0xCA);
    ASSERT(bytes[2] == 0xEB);
    ASSERT(bytes[3] == 0xD0);
}

void main() {
    test_int16();
    test_uint16();

}