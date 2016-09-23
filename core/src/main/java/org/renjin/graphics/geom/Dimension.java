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
package org.renjin.graphics.geom;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;

public class Dimension {
  private final double width;
  private final double height;
  
  public Dimension(double width, double height) {
    super();
    this.width = width;
    this.height = height;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }
  
  public double getAspectRatio() {
    return height / width;
  }

  public Dimension multiplyBy(Dimension b) {
    return new Dimension(width * b.getWidth(), height * b.getHeight());
  }

  public DoubleVector toVector() {
    return new DoubleArrayVector(width, height);
  }
}
