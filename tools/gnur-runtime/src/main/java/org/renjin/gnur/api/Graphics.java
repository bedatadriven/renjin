// Initial template generated from Graphics.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class Graphics {

  private Graphics() { }



  // Rboolean GRecording (SEXP, pGEDevDesc)

  // void Rf_GInit (GPar *)

  // void Rf_copyGPar (GPar *, GPar *)

  public static double R_Log10(double p0) {
     throw new UnimplementedGnuApiMethod("R_Log10");
  }

  // void Rf_ProcessInlinePars (SEXP, pGEDevDesc)

  // void Rf_recordGraphicOperation (SEXP, SEXP, pGEDevDesc)

  // SEXP Rf_FixupCol (SEXP, unsigned int)

  public static SEXP Rf_FixupLty(SEXP p0, int p1) {
     throw new UnimplementedGnuApiMethod("Rf_FixupLty");
  }

  public static SEXP Rf_FixupLwd(SEXP p0, double p1) {
     throw new UnimplementedGnuApiMethod("Rf_FixupLwd");
  }

  public static SEXP Rf_FixupVFont(SEXP p0) {
     throw new UnimplementedGnuApiMethod("Rf_FixupVFont");
  }

  public static SEXP Rf_labelformat(SEXP p0) {
     throw new UnimplementedGnuApiMethod("Rf_labelformat");
  }

  // void gcontextFromGP (pGEcontext gc, pGEDevDesc dd)

  // GPar* Rf_gpptr (pGEDevDesc dd)

  // GPar* Rf_dpptr (pGEDevDesc dd)
}
