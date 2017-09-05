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
// Initial template generated from GraphicsEngine.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.primitives.print.IntPrinter;
import org.renjin.sexp.SEXP;

import java.lang.invoke.MethodHandle;

/**
 * GNU R API methods defined in the "R_ext/GraphicsEngine.h" header file
 */
@SuppressWarnings("unused")
public final class GraphicsEngine {

  public static final int LTY_BLANK	= -1;
  public static final int LTY_SOLID	= 0;
  public static final int LTY_DASHED = 4 + (4<<4);
  public static final int LTY_DOTTED = 1 + (3<<4);
  public static final int LTY_DOTDASH =	1 + (3<<4) + (4<<8) + (3<<12);
  public static final int LTY_LONGDASH = 7 + (3<<4);
  public static final int LTY_TWODASH =	2 + (2<<4) + (6<<8) + (2<<12);

  private GraphicsEngine() { }

  public static int R_GE_getVersion() {
    throw new UnimplementedGnuApiMethod("R_GE_getVersion");
  }

  public static void R_GE_checkVersionOrDie(int version) {
    throw new UnimplementedGnuApiMethod("R_GE_checkVersionOrDie");
  }

  // pGEDevDesc Rf_desc2GEDesc (pDevDesc dd)

  // int GEdeviceNumber (pGEDevDesc)

  // pGEDevDesc GEgetDevice (int)

  // void GEaddDevice (pGEDevDesc)

  // void GEaddDevice2 (pGEDevDesc, const char *)

  // void GEaddDevice2f (pGEDevDesc, const char *, const char *)

  // void GEkillDevice (pGEDevDesc)

  // pGEDevDesc GEcreateDevDesc (pDevDesc dev)

  // void GEdestroyDevDesc (pGEDevDesc dd)

  // void* GEsystemState (pGEDevDesc dd, int index)

  // void GEregisterWithDevice (pGEDevDesc dd)

  public static void GEregisterSystem (MethodHandle callback, IntPtr systemRegisterIndex) {
    // NOOP
  }

  public static void GEunregisterSystem(int registerIndex) {
    throw new UnimplementedGnuApiMethod("GEunregisterSystem");
  }

  // SEXP GEhandleEvent (GEevent event, pDevDesc dev, SEXP data)

  // double GEfromDeviceX (double value, GEUnit to, pGEDevDesc dd)

  // double GEtoDeviceX (double value, GEUnit from, pGEDevDesc dd)

  // double GEfromDeviceY (double value, GEUnit to, pGEDevDesc dd)

  // double GEtoDeviceY (double value, GEUnit from, pGEDevDesc dd)

  // double GEfromDeviceWidth (double value, GEUnit to, pGEDevDesc dd)

  // double GEtoDeviceWidth (double value, GEUnit from, pGEDevDesc dd)

  // double GEfromDeviceHeight (double value, GEUnit to, pGEDevDesc dd)

  // double GEtoDeviceHeight (double value, GEUnit from, pGEDevDesc dd)

  // rcolor Rf_RGBpar (SEXP, int)

  // rcolor Rf_RGBpar3 (SEXP, int, rcolor)

  // const char* Rf_col2name (rcolor col)

  // rcolor R_GE_str2col (const char *s)

  // R_GE_lineend GE_LENDpar (SEXP value, int ind)

  // SEXP GE_LENDget (R_GE_lineend lend)

  // R_GE_linejoin GE_LJOINpar (SEXP value, int ind)

  // SEXP GE_LJOINget (R_GE_linejoin ljoin)

  // void GESetClip (double x1, double y1, double x2, double y2, pGEDevDesc dd)

  // void GENewPage (const pGEcontext gc, pGEDevDesc dd)

  // void GELine (double x1, double y1, double x2, double y2, const pGEcontext gc, pGEDevDesc dd)

  // void GEPolyline (int n, double *x, double *y, const pGEcontext gc, pGEDevDesc dd)

  // void GEPolygon (int n, double *x, double *y, const pGEcontext gc, pGEDevDesc dd)

  // SEXP GEXspline (int n, double *x, double *y, double *s, Rboolean open, Rboolean repEnds, Rboolean draw, const pGEcontext gc, pGEDevDesc dd)

  // void GECircle (double x, double y, double radius, const pGEcontext gc, pGEDevDesc dd)

  // void GERect (double x0, double y0, double x1, double y1, const pGEcontext gc, pGEDevDesc dd)

  // void GEPath (double *x, double *y, int npoly, int *nper, Rboolean winding, const pGEcontext gc, pGEDevDesc dd)

  // void GERaster (unsigned int *raster, int w, int h, double x, double y, double width, double height, double angle, Rboolean interpolate, const pGEcontext gc, pGEDevDesc dd)

  // SEXP GECap (pGEDevDesc dd)

  // void GEText (double x, double y, const char *const str, cetype_t enc, double xc, double yc, double rot, const pGEcontext gc, pGEDevDesc dd)

  // void GEMode (int mode, pGEDevDesc dd)

  // void GESymbol (double x, double y, int pch, double size, const pGEcontext gc, pGEDevDesc dd)

  public static void GEPretty(DoublePtr lo, DoublePtr up, IntPtr ndiv) {
    throw new UnimplementedGnuApiMethod("GEPretty");
  }

  // void GEMetricInfo (int c, const pGEcontext gc, double *ascent, double *descent, double *width, pGEDevDesc dd)

  // double GEStrWidth (const char *str, cetype_t enc, const pGEcontext gc, pGEDevDesc dd)

  // double GEStrHeight (const char *str, cetype_t enc, const pGEcontext gc, pGEDevDesc dd)

  // void GEStrMetric (const char *str, cetype_t enc, const pGEcontext gc, double *ascent, double *descent, double *width, pGEDevDesc dd)

  public static int GEstring_to_pch(SEXP pch) {
    throw new UnimplementedGnuApiMethod("GEstring_to_pch");
  }

  // unsigned int GE_LTYpar (SEXP, int)

  // SEXP GE_LTYget (unsigned int)

  // void R_GE_rasterScale (unsigned int *sraster, int sw, int sh, unsigned int *draster, int dw, int dh)

  // void R_GE_rasterInterpolate (unsigned int *sraster, int sw, int sh, unsigned int *draster, int dw, int dh)

  public static void R_GE_rasterRotatedSize(int w, int h, double angle, IntPtr wnew, IntPtr hnew) {
    throw new UnimplementedGnuApiMethod("R_GE_rasterRotatedSize");
  }

  public static void R_GE_rasterRotatedOffset(int w, int h, double angle, int botleft, DoublePtr xoff, DoublePtr yoff) {
    throw new UnimplementedGnuApiMethod("R_GE_rasterRotatedOffset");
  }

  // void R_GE_rasterResizeForRotation (unsigned int *sraster, int w, int h, unsigned int *newRaster, int wnew, int hnew, const pGEcontext gc)

  // void R_GE_rasterRotate (unsigned int *sraster, int w, int h, double angle, unsigned int *draster, const pGEcontext gc, Rboolean perPixelAlpha)

  // double GEExpressionWidth (SEXP expr, const pGEcontext gc, pGEDevDesc dd)

  // double GEExpressionHeight (SEXP expr, const pGEcontext gc, pGEDevDesc dd)

  // void GEExpressionMetric (SEXP expr, const pGEcontext gc, double *ascent, double *descent, double *width, pGEDevDesc dd)

  // void GEMathText (double x, double y, SEXP expr, double xc, double yc, double rot, const pGEcontext gc, pGEDevDesc dd)

  public static SEXP GEcontourLines(DoublePtr x, int nx, DoublePtr y, int ny, DoublePtr z, DoublePtr levels, int nl) {
    throw new UnimplementedGnuApiMethod("GEcontourLines");
  }

  // double R_GE_VStrWidth (const char *s, cetype_t enc, const pGEcontext gc, pGEDevDesc dd)

  // double R_GE_VStrHeight (const char *s, cetype_t enc, const pGEcontext gc, pGEDevDesc dd)

  // void R_GE_VText (double x, double y, const char *const s, cetype_t enc, double x_justify, double y_justify, double rotation, const pGEcontext gc, pGEDevDesc dd)

  // pGEDevDesc GEcurrentDevice (void)

  // Rboolean GEdeviceDirty (pGEDevDesc dd)

  // void GEdirtyDevice (pGEDevDesc dd)

  // Rboolean GEcheckState (pGEDevDesc dd)

  // Rboolean GErecording (SEXP call, pGEDevDesc dd)

  // void GErecordGraphicOperation (SEXP op, SEXP args, pGEDevDesc dd)

  // void GEinitDisplayList (pGEDevDesc dd)

  // void GEplayDisplayList (pGEDevDesc dd)

  public static void GEcopyDisplayList(int fromDevice) {
    throw new UnimplementedGnuApiMethod("GEcopyDisplayList");
  }

  // SEXP GEcreateSnapshot (pGEDevDesc dd)

  // void GEplaySnapshot (SEXP snapshot, pGEDevDesc dd)

  public static void GEonExit() {
    throw new UnimplementedGnuApiMethod("GEonExit");
  }

  public static void GEnullDevice() {
    throw new UnimplementedGnuApiMethod("GEnullDevice");
  }

  public static SEXP Rf_CreateAtVector(DoublePtr p0, DoublePtr p1, int p2, boolean p3) {
    throw new UnimplementedGnuApiMethod("Rf_CreateAtVector");
  }

  public static void Rf_GAxisPars(DoublePtr min, DoublePtr max, IntPtr n, boolean log, int axis) {
    throw new UnimplementedGnuApiMethod("Rf_GAxisPars");
  }
}
