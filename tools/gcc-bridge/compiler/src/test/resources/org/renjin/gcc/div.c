
#include <stdlib.h>

#include "assert.h"

void test() {
    div_t result;
     
    result = div(27, 4);
    ASSERT(result.quot == 6);
    ASSERT(result.rem == 3);
    
    result = div(27, 3);
    ASSERT(result.quot == 9);
    ASSERT(result.rem == 3);
}