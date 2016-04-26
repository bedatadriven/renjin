
#include "mynative.h"
#include "R_ext/Rdynload.h"

static const R_CMethodDef Centries[] = {
    {"Cmysum",     (DL_FUNC) &mysum,  1},
    { NULL, NULL, 0 }
};

R_init_native(DllInfo *dll)
{
    R_registerRoutines(dll, NULL, Centries, NULL, NULL);
    R_useDynamicSymbols(dll, FALSE);

}
