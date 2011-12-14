package r.compiler.ir.tac.operand;

import java.util.Collections;
import java.util.Set;

import r.lang.Context;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class Variable implements LValue, SimpleExpr {

  private final Symbol name;
  
  public Variable(Symbol name) {
    this.name = name;
  }
  
  public Variable(String name) {
    this(Symbol.get(name));
  }
  
  public Symbol getName() {
    return name;
  }
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    SEXP value = context.getEnvironment().getVariable(name);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + name + "' not found");
    }
    return value;
  }
  
  @Override
  public void setValue(Context context, Object[] temp, Object value) {
    context.getEnvironment().setVariable(name, (SEXP)value); 
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public Set<Variable> variables() {
    return Collections.singleton(this);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Variable other = (Variable) obj;
    return name == other.name;
  }
}
