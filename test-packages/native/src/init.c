
#include "mynative.h"

#ifdef MY_CPP_DEFINE

static const R_CallMethodDef callMethods[] = {
    {"Cmysum",     (DL_FUNC) &Cmysum,  1},
    {"Cmydsum",     (DL_FUNC) &Cmydsum,  1},
    { NULL, NULL, 0 }
};

static const R_FortranMethodDef Fentries[] = {
    {"Fdpchim",     (DL_FUNC) &dpchimtest_, 0},
    { NULL, NULL, 0 }
};

R_init_native(DllInfo *dll)
{
//    
//    // Verify that we can invoke API methods that require context
    SEXP stats = mkString("stats");
    SEXP statsNamespace = R_FindNamespace(stats);

    R_registerRoutines(dll, NULL, callMethods, Fentries, NULL);
    R_RegisterCCallable("native", "Cmysum", (DL_FUNC) &Cmysum);
    R_RegisterCCallable("native", "Cmydsum", (DL_FUNC) &Cmydsum);
    R_useDynamicSymbols(dll, FALSE);

}

#endif