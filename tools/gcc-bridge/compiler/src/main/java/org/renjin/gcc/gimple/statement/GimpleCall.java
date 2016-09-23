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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

public class GimpleCall extends GimpleStatement {

  private GimpleExpr function;
  private List<GimpleExpr> operands = Lists.newArrayList();
  private GimpleLValue lhs;

  public GimpleExpr getFunction() {
    return function;
  }

  @Override
  @JsonProperty("arguments")
  public List<GimpleExpr> getOperands() {
    return operands;
  }


  public GimpleExpr getOperand(int i) {
    return operands.get(i);
  }
  
  public GimpleLValue getLhs() {
    return lhs;
  }

  public void setFunction(GimpleExpr function) {
    this.function = function;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }

  public boolean isFunctionNamed(String name) {
    if(function instanceof GimpleFunctionRef) {
      GimpleFunctionRef ref = (GimpleFunctionRef) function;
      return ref.getName().equals(name);
    }
    return false;
  }

  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    function.findOrDescend(predicate, results);
    for (GimpleExpr argument : operands) {
      argument.findOrDescend(predicate, results);
    }
    // if the lhs is a compound expression, such as
    //    *x  = f() or
    //    x.i = f() or
    // Re(x)  = f()
    // 
    // then we consider this a USE of x rather than a definition

    if(lhs != null && !(lhs instanceof GimpleSymbolRef)) {
      lhs.find(predicate, results);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(lhs);
    sb.append(" = ");
    sb.append("gimple_call <").append(function).append(", ");
    Joiner.on(", ").appendTo(sb, operands);
    sb.append(">");
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitCall(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    if(lhs != null) {
      return predicate.apply(lhs);
    } else {
      return false;
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(lhs != null) {
      if (predicate.apply(lhs)) {
        lhs = (GimpleLValue) newExpr;
      } else {
        lhs.replaceAll(predicate, newExpr);
      }
    }
    if(predicate.apply(function)) {
      function = newExpr;
    } else {
      function.replaceAll(predicate, newExpr);
    }
    replaceAll(predicate, operands, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    if(lhs != null) {
      lhs.accept(visitor);
    }
    function.accept(visitor);
    for (GimpleExpr operand : operands) {
      operand.accept(visitor);
    }
  }

}
