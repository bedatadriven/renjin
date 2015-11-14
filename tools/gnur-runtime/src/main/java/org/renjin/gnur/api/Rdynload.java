// Initial template generated from Rdynload.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.primitives.packaging.DllInfo;

@SuppressWarnings("unused")
public final class Rdynload {

  private Rdynload() { }



  public static int R_registerRoutines (DllInfo info, 
                                        ObjectPtr<MethodDef> croutines,
                                        ObjectPtr<MethodDef> callRoutines,
                                        ObjectPtr<MethodDef> fortranRoutines,
                                        ObjectPtr<MethodDef> externalRoutines) {
    
    return 0;
    
  }
//
//   Rboolean R_useDynamicSymbols (DllInfo *info, Rboolean value)
//
//   Rboolean R_forceSymbols (DllInfo *info, Rboolean value)
//
//   DllInfo* R_getDllInfo (const char *name)
//
//   DllInfo* R_getEmbeddingDllInfo (void)
//
//   DL_FUNC R_FindSymbol (char const *, char const *, R_RegisteredNativeSymbol *symbol)
//
//   void R_RegisterCCallable (const char *package, const char *name, DL_FUNC fptr)
//
//   DL_FUNC R_GetCCallable (const char *package, const char *name)
}
