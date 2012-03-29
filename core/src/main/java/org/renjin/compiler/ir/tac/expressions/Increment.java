package org.renjin.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


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

  @Override  
  public Expression replaceVariable(Variable variable, Variable newVariable) {
    return new Increment( counter.replaceVariable(variable, newVariable));
  }

  @Override
  public List<Expression> getChildren() {
    return Arrays.asList((Expression)counter);
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i==0) {
      counter = (LValue) expr;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitIncrement(this);
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  }
}
