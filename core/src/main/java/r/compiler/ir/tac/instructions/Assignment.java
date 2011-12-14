package r.compiler.ir.tac.instructions;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.LValue;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;


public class Assignment implements Statement {
  private Operand rvalue;
  private LValue target;
 
  public Assignment(LValue target, Operand rvalue) {
    this.target = target;
    this.rvalue = rvalue;
  }
 
  public LValue getTarget() {
    return target;
  }
  
  public Operand getRValue() {
    return rvalue;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    target.setValue(context, temp, rvalue.retrieveValue(context, temp));
    return null;
  }
  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public Set<Variable> variables() {
    return Sets.union(target.variables(), rvalue.variables());
  }

  @Override 
  public String toString() {
    return getTarget() + " \u2190 " + rvalue;
  }
}
