
#include "mylink.h"

#ifdef MY_CPP_DEFINE

static const R_CallMethodDef callMethods[] = {
    {"Cmysum2",     (DL_FUNC) &Cmysum2,  1},
    {"Cmydsum2",     (DL_FUNC) &Cmydsum2,  1},
    { NULL, NULL, 0 }
};

R_init_native(DllInfo *dll)
{
    R_registerRoutines(dll, NULL, callMethods, NULL, NULL);
    R_useDynamicSymbols(dll, FALSE);
}

#endif