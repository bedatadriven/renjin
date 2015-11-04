// Initial template generated from RS.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class RS {

  private RS() { }



  public static Ptr R_chk_calloc(/*size_t*/ int p0, /*size_t*/ int p1) {
     throw new UnimplementedGnuApiMethod("R_chk_calloc");
  }

  public static Ptr R_chk_realloc(Ptr p0, /*size_t*/ int p1) {
     throw new UnimplementedGnuApiMethod("R_chk_realloc");
  }

  public static void R_chk_free(Ptr p0) {
     throw new UnimplementedGnuApiMethod("R_chk_free");
  }

  // void call_R (char *, long, void **, char **, long *, char **, long, char **)
}
