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
// Initial template generated from stats_stubs.h from R 3.2.2
package org.renjin.gnur.api;

@SuppressWarnings("unused")
public final class stats_stubs {

  private stats_stubs() { }



  public static void S_Rf_divset(int alg, int iv, int liv, int lv, double v) {
    throw new UnimplementedGnuApiMethod("S_Rf_divset");
  }

  public static void S_nlminb_iterate(double b, double d, double fx, double g, double h, int iv, int liv, int lv, int n, double v, double x) {
    throw new UnimplementedGnuApiMethod("S_nlminb_iterate");
  }

  public static void S_nlsb_iterate(double b, double d, double dr, int iv, int liv, int lv, int n, int nd, int p, double r, double rd, double v, double x) {
    throw new UnimplementedGnuApiMethod("S_nlsb_iterate");
  }
}
