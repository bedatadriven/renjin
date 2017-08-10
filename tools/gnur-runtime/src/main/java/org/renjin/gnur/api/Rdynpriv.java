/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Rdynpriv.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;

@SuppressWarnings("unused")
public final class Rdynpriv {

  private Rdynpriv() { }



  public static int R_moduleCdynload(BytePtr module, int local, int now) {
    throw new UnimplementedGnuApiMethod("R_moduleCdynload");
  }

  // DL_FUNC Rf_lookupCachedSymbol (const char *name, const char *pkg, int all)

  // DL_FUNC R_dlsym (DllInfo *info, char const *name, R_RegisteredNativeSymbol *symbol)

  // SEXP R_MakeExternalPtrFn (DL_FUNC p, SEXP tag, SEXP prot)

  // DL_FUNC R_ExternalPtrAddrFn (SEXP s)

  // DL_FUNC R_dotCallFn (SEXP, SEXP, int)

  // SEXP R_doDotCall (DL_FUNC, int, SEXP *, SEXP)
}
