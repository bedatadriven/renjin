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
package org.renjin.gcc;

import org.renjin.gcc.runtime.OpaquePtr;
import org.renjin.gcc.runtime.RecordUnitPtr;

/**
 * An existing JVM class to which the record type jvm_rect will be mapped
 */
public class JvmRect {
  
  public int width;
  public int height;

  public JvmRect() {
  }

  public JvmRect(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  public void set(JvmRect o) {
    this.width = o.width;
    this.height = o.height;
  }

  public static int area(RecordUnitPtr<JvmRect> rect) {
    return rect.get().width * rect.get().height;
  }
  
  public static int areas(OpaquePtr<JvmRect> rects) {
    int area = 0;
    int i = 0;
    // end of array is marked by rect with zero width
    while(rects.get(i).width != 0) {
      area += (rects.get(i).width * rects.get(i).height);
      i++;
    }
    return area;
  }
}
