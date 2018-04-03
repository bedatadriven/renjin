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
// Initial template generated from GraphicsEngine.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.sexp.SEXP;

/**
 * GNU R API methods defined in the "R_ext/GraphicsEngine.h" header file
 */
@SuppressWarnings("unused")
public final class GraphicsEngine {

  private GraphicsEngine() { }

  @Deprecated
  public static int R_GE_getVersion() {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void R_GE_checkVersionOrDie(int version) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void GEPretty(DoublePtr lo, DoublePtr up, IntPtr ndiv) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static int GEstring_to_pch(SEXP pch) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void R_GE_rasterRotatedSize(int w, int h, double angle, IntPtr wnew, IntPtr hnew) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void R_GE_rasterRotatedOffset(int w, int h, double angle, int botleft, DoublePtr xoff, DoublePtr yoff) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static SEXP GEcontourLines(DoublePtr x, int nx, DoublePtr y, int ny, DoublePtr z, DoublePtr levels, int nl) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void GEcopyDisplayList(int fromDevice) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void GEonExit() {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void GEnullDevice() {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static SEXP Rf_CreateAtVector(DoublePtr p0, DoublePtr p1, int p2, boolean p3) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }

  @Deprecated
  public static void Rf_GAxisPars(DoublePtr min, DoublePtr max, IntPtr n, boolean log, int axis) {
    throw new RuntimeException("Please recompile with the latest version of Renjin.");
  }
}
