/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from GetX11Image.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.IntPtr;

/**
 * GNU R API methods defined in the "R_ext/GetX11Image.h" header file
 */
@SuppressWarnings("unused")
public final class GetX11Image {

  private GetX11Image() { }



  public static boolean R_GetX11Image(int d, Object pximage, IntPtr pwidth, IntPtr pheight) {
    throw new UnimplementedGnuApiMethod("R_GetX11Image");
  }
}
