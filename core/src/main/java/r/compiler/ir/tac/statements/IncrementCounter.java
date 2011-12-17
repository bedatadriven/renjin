package r.compiler.ir.tac.statements;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.expressions.Variable;
import r.lang.Context;

/**
 * Increments a counter variable. Only used for the 
 * 'for' loop, will see if really need this
 * 
 */
public class IncrementCounter implements Statement {

  private LValue counter;

  public IncrementCounter(LValue counter) {
    this.counter = counter;
  }
  
  public LValue getCounter() {
    return counter;
  }
 
  @Override
  public Object interpret(Context context, Object[] temp) {
    Integer i = (Integer) counter.retrieveValue(context, temp);
    counter.setValue(context, temp, i+1);
    return null;
  }
  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public Set<Variable> variables() {
    return counter.variables();
  }
  
  @Override
  public Expression getRHS() {
    return counter;
  }
  
  @Override
  public Statement withRHS(Expression newRHS) {
    if(!(newRHS instanceof LValue)) {
      throw new IllegalArgumentException("IncrementCounter requires temp lvalue");
    }
    return new IncrementCounter((LValue) newRHS);
  }

  @Override
  public String toString() {
    return "increment counter " + counter;
  }

}
