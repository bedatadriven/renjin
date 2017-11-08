
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "assert.h"

struct header {
    int version;
    char prod_name[10];
    double q;
};

void check_hdr(struct header hdr) {

    char name[10];

    memcpy (name, &hdr.prod_name, 10);

    ASSERT(strcmp(name, "foobar") == 0);
}

void test_assign() {

    struct header hdr;
    hdr.version = 4;
    strcpy(&hdr.prod_name[0], "foobar");

    check_hdr(hdr);
}

void main() {
   test_assign();
}