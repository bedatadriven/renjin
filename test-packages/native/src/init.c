
#include "mynative.h"
#include "R_ext/Rdynload.h"

#ifdef MY_CPP_DEFINE

static const R_CMethodDef Centries[] = {
    {"Cmysum",     (DL_FUNC) &mysum,  1},
    { NULL, NULL, 0 }
};

static const R_FortranMethodDef Fentries[] = {
    {"Fdpchim",     (DL_FUNC) &dpchimtest_, 0},
    { NULL, NULL, 0 }
};

R_init_native(DllInfo *dll)
{
    R_registerRoutines(dll, NULL, Centries, Fentries, NULL);
    R_useDynamicSymbols(dll, FALSE);

}

#endif