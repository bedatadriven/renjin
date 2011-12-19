package r.compiler.ir.tac.expressions;

import java.util.Set;

import r.lang.Context;

/**
 * Increments a counter variable. Only used for the 
 * 'for' loop, will see if really need this
 * 
 */
public class Increment implements Expression {

  private LValue counter;

  public Increment(LValue counter) {
    this.counter = counter;
  }
  
  public LValue getCounter() {
    return counter;
  }
   
  @Override
  public Set<Variable> variables() {
    return counter.variables();
  }

  @Override
  public String toString() {
    return "increment counter " + counter;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    Integer counterValue = (Integer) counter.retrieveValue(context, temps);
    return counterValue + 1;
  }

  @Override  public Expression replaceVariable(Variable variable, Variable newVariable) {
    return new Increment( counter.replaceVariable(variable, newVariable));
  }
}
