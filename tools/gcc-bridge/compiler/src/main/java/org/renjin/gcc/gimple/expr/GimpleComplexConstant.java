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
package org.renjin.gcc.gimple.expr;


import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleComplexConstant extends GimpleConstant {

  private GimpleRealConstant real;
  private GimpleRealConstant im;

  public GimpleRealConstant getReal() {
    return real;
  }

  public void setReal(GimpleRealConstant real) {
    this.real = real;
  }

  public GimpleRealConstant getIm() {
    return im;
  }

  public void setIm(GimpleRealConstant im) {
    this.im = im;
  }

  @Override
  public String toString() {
    return real + "+" + im + "i";
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    real = (GimpleRealConstant) replaceOrDescend(real, predicate, newExpr);
    im = (GimpleRealConstant) replaceOrDescend(im, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitComplexConstant(this);
  }
}

