package r.compiler.ir.tac.instructions;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.Temp;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

/**
 * Increments a counter variable. Only used for the 
 * 'for' loop, will see if really need this
 * 
 */
public class IncrementCounter implements Statement {

  private Temp counter;

  public IncrementCounter(Temp counter) {
    this.counter = counter;
  }
  
  public Temp getCounter() {
    return counter;
  }
 
  @Override
  public Object interpret(Context context, Object[] temp) {
    Integer i = (Integer)temp[counter.getIndex()];
    temp[counter.getIndex()] = i+1;
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
  public String toString() {
    return "increment counter " + counter;
  }
}
