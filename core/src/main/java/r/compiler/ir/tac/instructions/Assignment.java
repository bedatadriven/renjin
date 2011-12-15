package r.compiler.ir.tac.instructions;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

import com.google.common.collect.Sets;


public class Assignment implements Statement {
  private Variable lhs;
  private Operand rhs;
 
  public Assignment(Variable lhs, Operand rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
 
  public Variable getLHS() {
    return lhs;
  }
 
  @Override
  public Operand getRHS() {
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
  public Assignment withRHS(Operand newRHS) {
    return new Assignment(lhs, newRHS);
  }

  public Statement withLHS(Variable lhs) {
    return new Assignment(lhs, rhs);
  }

  @Override 
  public String toString() {
    return getLHS() + " \u2190 " + rhs;
  }
}
