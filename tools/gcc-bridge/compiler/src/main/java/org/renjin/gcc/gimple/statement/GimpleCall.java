/*
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
package org.renjin.gcc.gimple.statement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;
import java.util.function.Predicate;

public class GimpleCall extends GimpleStatement {

  private GimpleExpr function;
  private List<GimpleExpr> operands = Lists.newArrayList();
  private GimpleLValue lhs;

  public GimpleExpr getFunction() {
    return function;
  }

  @JsonProperty("arguments")
  public List<GimpleExpr> getOperands() {
    return operands;
  }


  public GimpleExpr getOperand(int i) {
    return operands.get(i);
  }


  public void setOperand(int i, GimpleExpr op) {
    operands.set(i, op);
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
    return name.equals(getFunctionName());
  }

  /**
   * @return the name of the function if this is a static call, or "" if this is a call to a function pointer.
   */
  public String getFunctionName() {
    if(function instanceof GimpleAddressOf) {
      GimpleExpr value = ((GimpleAddressOf) function).getValue();
      if (value instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) value;
        return ref.getName();
      }
    }
    return "";
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
    sb.append(function).append("(");
    Joiner.on(", ").appendTo(sb, operands);
    sb.append(")");
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitCall(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    if(lhs != null) {
      return predicate.test(lhs);
    } else {
      return false;
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(lhs != null) {
      if (predicate.test(lhs)) {
        lhs = (GimpleLValue) newExpr;
      } else {
        lhs.replaceAll(predicate, newExpr);
      }
    }
    if(predicate.test(function)) {
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
