/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
// Initial template generated from Graphics.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;

/**
 * GNU R API methods defined in the "Graphics.h" header file
 */
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
