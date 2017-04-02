
#include <math.h>


static int my_mutable_global = 42;

void test_global_var_doesnt_get_pruned() {

    static int x = my_mutable_global;

    ASSERT(x == 42);

}