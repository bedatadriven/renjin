package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


/**
 * An unevaluated argument to a dynamic function call. 
 * (it will become a promise at runtime)
 *
 */
public class IRThunk implements SimpleExpression {
  
  /**
   * The original expression
   */
  private SEXP sexp;
  
  /**
   * The body of the expression that will evaluate to the argument
   */
  private IRBody thunkBody;

  public IRThunk(SEXP exp, IRBody thunkBody) {
    this.sexp = exp;
    this.thunkBody = thunkBody;
  }
  
  public SEXP getSEXP() {
    return sexp;
  }

  @Override
  public SEXP retrieveValue(Context context, Object[] temps) {
    return thunkBody.evaluate(context);
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitPromise(this);
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public SimpleExpression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public String toString() {
    return "UNEVALED[ " + sexp + " ]";
  }

  @Override
  public SEXP getSExpression() {
    return sexp;
  }
  
  public IRBody getBody() {
    return thunkBody;
  }
}
