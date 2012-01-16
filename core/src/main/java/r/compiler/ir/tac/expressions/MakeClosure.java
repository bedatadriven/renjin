package r.compiler.ir.tac.expressions;


import java.util.Collections;
import java.util.List;
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
  
  public IRFunction getFunction() {
    return function;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }
  
  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return "closure(" + function.toString() + ")";
  }
}
