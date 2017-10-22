
#include <inttypes.h>
#include <stdlib.h>
#include <stdio.h>

#include "assert.h"

typedef u_int64_t sha2_word64;	/* Exactly 8 bytes */

sha2_word64 rotate(u_int64_t x) {
    sha2_word64 tmp = x;
	tmp = (tmp >> 32) | (tmp << 32);
    printf("%lx = %lx\n", x, tmp);
    return tmp;
}

void test_rotate_long() {
    ASSERT(rotate(0x1) == 0x100000000ULL);
    ASSERT(rotate(0x1234567) == 0x123456700000000ULL);
    ASSERT(rotate(0xcafebabe0123456ULL) == 0xe01234560cafebabULL);
}


int first_digit() {
    u_int8_t d = 0xef;
    u_int8_t d1 = (d >> 4);
    return d1;
}

void test_shift_byte() {
    int x = first_digit();
    int y = 14;
    ASSERT(x == y);
    printf("d = %x\n", first_digit());
    ASSERT(first_digit(0xEF) == 14);
}


void main() {
    test_rotate_long();
    test_shift_byte();
}
