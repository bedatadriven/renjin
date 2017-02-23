
#include <stdlib.h>
#include "assert.h"

void test_strcpy() {

    const char *src = "Hello brave world!";
    char dest[100];

    strcpy(dest, src);

    ASSERT(dest[0] == 'H');

}