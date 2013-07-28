package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;


/**
 * When used within an argument list, the elipses symbol
 * (...) indicates that the unmatched arguments from the calling
 * function should be merged into the argument list to the function call.
 * 
 * <code>
 * f <- function(...) list(...)
 * x <- f(1,2,3)  
 * </code>
 * 
 */
public class Elipses implements SimpleExpression {

  public static final Elipses INSTANCE = new Elipses();
  
  private Elipses() {
    
  }
  
  private Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    
  }

  @Override
  public SimpleExpression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public String toString() {
    return "...";
  }
}
