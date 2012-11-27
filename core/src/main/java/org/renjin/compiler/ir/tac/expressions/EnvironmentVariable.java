package org.renjin.compiler.ir.tac.expressions;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A {@code Variable} that is bound to the R {@code Environment}.
 */
public class EnvironmentVariable implements Variable {

  private final Symbol name;
  
  public EnvironmentVariable(Symbol name) {
    this.name = name;
  }
  
  public EnvironmentVariable(String name) {
    this(Symbol.get(name));
  }
  
  public Symbol getName() {
    return name;
  }
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    SEXP value = context.getEnvironment().findVariable(name);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + name + "' not found");
    }
    return value.force(context);
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
    return Collections.<Variable>singleton(this);
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
    EnvironmentVariable other = (EnvironmentVariable) obj;
    return name == other.name;
  }

  @Override
  public Variable replaceVariable(Variable name, Variable newName) {
    if(this.equals(name)) {
      return newName;
    } else {
      return this;
    }
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
  public void accept(ExpressionVisitor visitor) {
    visitor.visitEnvironmentVariable(this);
  }

  @Override
  public SEXP getSExpression() {
    return name;
  }
}
