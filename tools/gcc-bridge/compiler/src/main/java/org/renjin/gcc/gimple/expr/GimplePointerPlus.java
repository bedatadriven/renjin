/**
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
package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.List;
import java.util.Objects;

public class GimplePointerPlus extends GimpleExpr {

  private GimpleExpr pointer;
  private GimpleExpr offset;

  public GimplePointerPlus() {
  }

  public GimplePointerPlus(GimpleExpr pointer, GimpleExpr offset) {
    this.pointer = pointer;
    this.offset = offset;
    setType(pointer.getType());
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

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    pointer.find(predicate, results);
    offset.find(predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    pointer = replaceOrDescend(pointer, predicate, newExpr);
    offset = replaceOrDescend(offset, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitPointerPlus(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimplePointerPlus that = (GimplePointerPlus) o;

    return Objects.equals(this.pointer, that.pointer) &&
           Objects.equals(this.offset, that.offset);
  }

  @Override
  public int hashCode() {
    int result = pointer != null ? pointer.hashCode() : 0;
    result = 31 * result + (offset != null ? offset.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return pointer + "+" + offset;
  }
}
