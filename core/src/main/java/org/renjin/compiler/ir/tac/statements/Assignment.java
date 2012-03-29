package org.renjin.compiler.ir.tac.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.IRUtils;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.eval.Context;


import com.google.common.collect.Sets;


public class Assignment implements Statement {
  private LValue lhs;
  private Expression rhs;
 
  public Assignment(LValue lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
 
  public LValue getLHS() {
    return lhs;
  }
 
  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    lhs.setValue(context, temp, rhs.retrieveValue(context, temp));
    return null;
  }
  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public Set<Variable> variables() {
    return Sets.union(lhs.variables(), rhs.variables());
  }
  
  @Override
  public Assignment withRHS(Expression newRHS) {
    return new Assignment(lhs, newRHS);
  }
  

  public Statement withLHS(Variable lhs) {
    return new Assignment(lhs, rhs);
  }

  @Override 
  public String toString() {
    return getLHS() + " " + IRUtils.LEFT_ARROW + " "  + rhs;
  }

  @Override
  public List<Expression> getChildren() {
    return Arrays.asList(rhs);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      rhs = child;
    } else {
      throw new IllegalArgumentException("childIndex=" + childIndex);
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitAssignment(this);
  }
}
