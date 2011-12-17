package r.compiler.ir.tac.expressions;


import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRFunction;
import r.lang.Context;

public class MakeClosure implements Expression {

  private IRFunction function;
  
  public MakeClosure(IRFunction function) {
    this.function = function;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return function.newClosure(context);
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public String toString() {
    return "closure(" + function.toString() + ")";
  }
}
