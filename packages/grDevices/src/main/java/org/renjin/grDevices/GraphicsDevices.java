/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.grDevices;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.RecordUnitPtr;
import org.renjin.gcc.runtime.Stdlib;

public class GraphicsDevices {

  public static Ptr newDevice() {
    return new RecordUnitPtr<>(new JavaGD());
  }

  public static void open(Ptr p, double w, double h) {
    ((JavaGD) p.getArray()).open(w, h);
  }

  public static void close(Ptr p) {
    ((JavaGD) p.getArray()).close();
  }

  public static void activate(Ptr p) {
    ((JavaGD) p.getArray()).activate();
  }

  public static void circle(Ptr p, double x, double y, double r) {
    ((JavaGD) p.getArray()).circle(x, y, r);
  }

  public static void clip(Ptr p,  double x0, double x1, double y0, double y1) {
    ((JavaGD) p.getArray()).clip(x0, x1, y0, y1);
  }

  public static void deactivate(Ptr p) {
    ((JavaGD) p.getArray()).deactivate();
  }

  public static void hold(Ptr p) {
    ((JavaGD) p.getArray()).hold();
  }

  public static void flush(Ptr p, boolean hold) {
    ((JavaGD) p.getArray()).flush(hold);
  }

  public static Ptr locator(Ptr p) {
    return new DoublePtr( ((GraphicsDevice) p.getArray()).locator() );
  }

  public static void line(Ptr p, double x1, double y1, double x2, double y2) {
    ((GraphicsDevice) p.getArray()).line(x1, y1, x2, y2);
  }

  public static Ptr metricInfo(Ptr p, int c) {
    return new DoublePtr( ((GraphicsDevice) p.getArray()).metricInfo(c) );
  }

  public static void mode(Ptr p, int mode) {
    ((GraphicsDevice) p.getArray()).mode(mode);
  }

  public static void newPage(Ptr p, int deviceNumber) {
    ((GraphicsDevice) p.getArray()).newPage(deviceNumber);
  }

  public static void path(Ptr p, int npoly, Ptr nper, Ptr x, Ptr y, boolean winding) {
    ((GraphicsDevice) p.getArray()).path(npoly, toIntArray(nper), toDoubleArray(x), toDoubleArray(y), winding);
  }

  public static void polygon(Ptr p, int n, Ptr x, Ptr y) {
    ((GraphicsDevice) p.getArray()).polygon(n, x, y);
  }

  public static void polyline(Ptr p, int n, Ptr x, Ptr y) {
    ((GraphicsDevice) p.getArray()).polyline(n, x, y);
  }

  public static void rect(Ptr p, double x0, double y0, double x1, double y1) {
    ((GraphicsDevice) p.getArray()).rect(x0, y0, x1, y1);
  }

  public static double[] size(Ptr p) {
    return ((GraphicsDevice) p.getArray()).size();
  }

  public static double strWidth(Ptr p, Ptr str) {
    return ((GraphicsDevice) p.getArray()).strWidth(Stdlib.nullTerminatedString(str));
  }

  public static void text(Ptr p, double x, double y, Ptr str, double rot, double hadj) {
    ((GraphicsDevice) p.getArray()).text(x, y, Stdlib.nullTerminatedString(str), rot, hadj);
  }

  public static void raster(Ptr p, Ptr raster, int w, int h, double x, double y, double width, double height, double rot, double interpolate) {
    throw new UnsupportedOperationException("TODO");
  }

  public static void setColor(Ptr p, int color) {
    ((GraphicsDevice) p.getArray()).setColor(color);
  }

  public static void setFill(Ptr p, int color) {
    ((GraphicsDevice) p.getArray()).setFill(color);
  }

  public static void setLine(Ptr p, double lwd, int lty) {
    ((GraphicsDevice) p.getArray()).setLine(lwd, lty);
  }

  public static void setFont(Ptr p, double cex, double ps, double lineheight, int fontface, Ptr fontfamily) {
    ((GraphicsDevice) p.getArray()).setFont(cex, ps, lineheight, fontface, Stdlib.nullTerminatedString(fontfamily));
  }

  private static double[] toDoubleArray(Ptr p) {
    throw new UnsupportedOperationException("TODO");
  }

  private static int[] toIntArray(Ptr p) {
    throw new UnsupportedOperationException("TODO");
  }

}
