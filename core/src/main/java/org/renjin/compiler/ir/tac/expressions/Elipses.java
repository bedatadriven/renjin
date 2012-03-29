package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;


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
  
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SEXP getSExpression() {
    return Symbols.ELLIPSES;
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
   
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
