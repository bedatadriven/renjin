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
package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import java.util.function.Predicate;

import java.util.List;

public class GimpleArrayRef extends GimpleLValue {
  private GimpleExpr array;
  private GimpleExpr index;

  public GimpleArrayRef() {
  }
  
  public GimpleArrayRef(GimpleExpr array, int index) {
    this.array = array;
    this.index = new GimpleIntegerConstant(GimpleIntegerType.unsigned(32), index);
    this.setType(((GimpleArrayType) array.getType()).getComponentType());
  }

  public GimpleArrayRef(GimpleExpr array, GimpleExpr index) {
    this.array = array;
    this.index = index;
  }

  public GimpleExpr getArray() {
    return array;
  }

  public void setValue(GimpleExpr value) {
    this.array = value;
  }

  public void setIndex(GimpleExpr index) {
    this.index = index;
  }

  public GimpleExpr getIndex() {
    return index;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(array, predicate, results);
    findOrDescend(index, predicate, results);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitArrayRef(this);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    array = replaceOrDescend(array, predicate, newExpr);
    index = replaceOrDescend(index, predicate, newExpr);
  }

  @Override
  public String toString() {
    return array + "[" + index + "]";
  }
}
