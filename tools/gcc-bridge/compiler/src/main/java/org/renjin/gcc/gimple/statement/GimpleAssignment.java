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

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import java.util.function.Predicate;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class GimpleAssignment extends GimpleStatement {
  private GimpleOp operator;
  private GimpleLValue lhs;
  private List<GimpleExpr> operands = Lists.newArrayList();

  public GimpleAssignment() {
  }

  public GimpleAssignment(GimpleOp op, GimpleLValue lhs, GimpleExpr... arguments) {
    this.operator = op;
    this.lhs = lhs;
    this.operands.addAll(Arrays.asList(arguments));
  }

  public GimpleOp getOperator() {
    return operator;
  }

  public void setOperator(GimpleOp op) {
    this.operator = op;
  }

  public GimpleLValue getLHS() {
    return lhs;
  }

  public List<GimpleExpr> getOperands() {
    return operands;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(lhs).append(" = ").append(operator.format(operands));
    if(getLineNumber() != null) {
      sb.append("\t\t\t#").append(getLineNumber());
      if(getSourceFile() != null) {
        sb.append(" (").append(getSourceFile()).append(")");
      }
    }

    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    return predicate.test(lhs);
  }


  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findUses(operands, predicate, results);
    
    // if the lhs is a compound expression, such as
    //    *x  = y or
    //    x.i = y or
    // Re(x)  = y
    // 
    // then we consider this a USE of x rather than a definition
    
    if(!(lhs instanceof GimpleSymbolRef)) {
      lhs.find(predicate, results);
    }
  }


  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.test(lhs)) {
      lhs = (GimpleLValue) newExpr;
    } else {
      lhs.replaceAll(predicate, newExpr);
    }
    replaceAll(predicate, operands, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    lhs.accept(visitor);
    for (GimpleExpr operand : operands) {
      operand.accept(visitor);
    }
  }
}
