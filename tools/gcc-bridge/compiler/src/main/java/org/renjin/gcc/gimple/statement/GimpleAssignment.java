package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
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

  @Override
  public List<GimpleExpr> getOperands() {
    return operands;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("gimple_assign<").append(operator).append(", ").append(lhs).append(", ");
    Joiner.on(", ").appendTo(sb, operands);
    sb.append(">");
    if(getLineNumber() != null) {
      sb.append("  #").append(getLineNumber());
    }
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    return predicate.apply(lhs);
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
    if(predicate.apply(lhs)) {
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
