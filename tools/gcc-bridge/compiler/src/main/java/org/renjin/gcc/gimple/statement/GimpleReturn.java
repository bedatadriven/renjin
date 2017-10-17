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
package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.Collections;
import java.util.List;

public class GimpleReturn extends GimpleStatement {
  private GimpleExpr value;

  public GimpleReturn() {
  }

  public GimpleReturn(GimpleExpr value) {
    this.value = value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public GimpleExpr getValue() {
    return value;
  }

  public List<GimpleExpr> getOperands() {
    if(value == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(value);
    }
  }

  @Override
  public String toString() {
    return "gimple_return <" + value + ">";
  }
  
  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    if(value != null) {
      value.findOrDescend(predicate, results);
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(value)) {
      value = newExpr;
    } else {
      value.replaceAll(predicate, newExpr);
    }
  }

  @Override
  public void acceptRight(GimpleExprVisitor visitor) {
    if(value != null) {
      value.accept(visitor);
    }
  }
}
