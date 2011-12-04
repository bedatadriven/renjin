package r.compiler.ir.tac.operand;

import r.lang.Context;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class Variable implements LValue, SimpleExpr {

  private final Symbol name;
  
  public Variable(Symbol name) {
    this.name = name;
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
}
