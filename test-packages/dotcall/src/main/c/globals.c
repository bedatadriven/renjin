
#include "proto.h"

static int evil_global_state = 1;


SEXP globalcount() 
{
    return ScalarInteger(evil_global_state++);
}