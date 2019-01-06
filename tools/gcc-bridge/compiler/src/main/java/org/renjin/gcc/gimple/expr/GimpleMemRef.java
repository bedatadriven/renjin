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
import java.util.function.Predicate;

import java.util.List;

public class GimpleMemRef extends GimpleLValue {

  private GimpleExpr pointer;
  private GimpleExpr offset;

  public GimpleMemRef() {
  }

  public GimpleMemRef(GimpleExpr pointer) {
    this.pointer = pointer;
    this.offset = new GimpleIntegerConstant();
    this.offset.setType(pointer.getType());
    setType(pointer.getType().getBaseType());
  }

  public GimpleExpr getPointer() {
    return pointer;
  }

  public void setPointer(GimpleExpr pointer) {
    this.pointer = pointer;
  }

  public GimpleExpr getOffset() {
    return offset;
  }

  public void setOffset(GimpleExpr offset) {
    this.offset = offset;
  }

  public String toString() {
    if(isOffsetZero()) {
      return "*" + pointer;
    } else {
      return "*(" + pointer + "+" + offset + ")";
    }
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(pointer, predicate, results);
    findOrDescend(offset, predicate, results);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitMemRef(this);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    pointer = replaceOrDescend(pointer, predicate, newExpr);
    offset = replaceOrDescend(offset, predicate, newExpr);
  }
  
  public boolean isOffsetZero() {
    return offset instanceof GimpleIntegerConstant && 
        ((GimpleIntegerConstant) offset).getNumberValue().intValue() == 0;
  }
}
