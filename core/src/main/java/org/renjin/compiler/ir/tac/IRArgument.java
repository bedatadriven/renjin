package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class IRArgument {

  private String name;
  private Expression expression;

  public IRArgument(String name, Expression expression) {
    this.name = name;
    this.expression = expression;
  }

  public IRArgument(SEXP name, SimpleExpression expression) {
    if(name == Null.INSTANCE) {
      this.name = null;
    } else if(name instanceof Symbol) {
      this.name = ((Symbol) name).getPrintName();
    } else {
      throw new IllegalArgumentException("name: " + name);
    }
    this.expression = expression;
  }

  public IRArgument(Expression expression) {
    this.name = null;
    this.expression = expression;
  }
  
  public boolean isNamed() {
    return name != null;
  }

  public String getName() {
    return name;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  
  public static boolean anyNamed(Iterable<IRArgument> arguments) {
    for (IRArgument argument : arguments) {
      if(argument.isNamed()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    if(isNamed()) {
      return name + " = " + expression;
    } else {
      return expression.toString();
    }
  }
}
