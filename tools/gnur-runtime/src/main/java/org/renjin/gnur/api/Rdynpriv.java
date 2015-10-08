// Initial template generated from Rdynpriv.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;

@SuppressWarnings("unused")
public final class Rdynpriv {

  private Rdynpriv() { }



  public static int R_moduleCdynload(CharPtr module, int local, int now) {
     throw new UnimplementedGnuApiMethod("R_moduleCdynload");
  }

  // DL_FUNC Rf_lookupCachedSymbol (const char *name, const char *pkg, int all)

  // DL_FUNC R_dlsym (DllInfo *info, char const *name, R_RegisteredNativeSymbol *symbol)

  // SEXP R_MakeExternalPtrFn (DL_FUNC p, SEXP tag, SEXP prot)

  // DL_FUNC R_ExternalPtrAddrFn (SEXP s)

  // DL_FUNC R_dotCallFn (SEXP, SEXP, int)

  // SEXP R_doDotCall (DL_FUNC, int, SEXP *, SEXP)
}
