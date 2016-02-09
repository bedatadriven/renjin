
/*
** This file causes the entry points of my .C routines to be preloaded
** It adds one more layer of protection by declaring the number of arguments,
**  and perhaps a tiny bit of speed
*/

#include "R_ext/Rdynload.h"
#include "proto.h"

//static const R_CMethodDef Centries[] = {
//    {"Cmysample",    (DL_FUNC) &mysample,  1},
//    {NULL, NULL, 0}
//};

static const R_CallMethodDef Callentries[] = {
    {"Cmysample",     (DL_FUNC) &mysample,     1},
    {"Cglobalcount",  (DL_FUNC) &globalcount,  0},
    {NULL, NULL, 0}
};

void R_init_dotcall(DllInfo *dll){
    R_registerRoutines(dll, NULL, Callentries, NULL, NULL);

    /* My take on the documentation is that adding the following line
       will make symbols available ONLY through the above tables.
       Anyone who then tried to link to my C code would be SOL.
       It also wouldn't work with .C("whatever", ....) which I use in
       my test directory.
    */
   /* R_useDynamicSymbols(dll, FALSE);  */
}
    