/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.sexp;

import org.renjin.eval.Context;

/**
 * Promises are the mechanism by which R implements lazy "call-by-need"
 * semantics for closures.
 *
 * Promises enable delayed evaluation in such a way that an expression
 * provided as an argument is evaluated only once, but only if it is
 * used.
 *
 * In Haskell and other lazy functional languages, a "Promise" would be called
 * a "thunk."
 *
 */
public class Promise extends AbstractSEXP implements Recursive {

  public static final String TYPE_NAME = "promise";

  protected Environment environment;
  protected SEXP expression;
  private SEXP result;

  protected Promise(Environment environment, SEXP expression) {
    assert environment != null;
    this.expression = expression;
    this.environment = environment;
  }

  /**
   * Creates a Promise with it's expression and its already-evaluated
   * result.
   * 
   * This may seem unnecesary, but in fact this is required because closures
   * expect to be able to access both the values and the "source" of their arguments
   * for labeling plots etc.
   * 
   * For example, when plot(sin(x)) is called, the interpreter needs to evaluate
   * sin(x) to determine its class and choose the correct plot function, but that plot 
   * function needs to be able to access the expression sin(x) to correctly label the y axis. 
   *
   * @param expression
   * @param result
   */
  public Promise(SEXP expression, SEXP result) {
    this.environment = Environment.EMPTY;
    this.expression = expression;
    this.result = result;
  }


  /**
   * Forces the evaluation of this Promise, if it has not already
   * been evaluated.
   *
   * @return the result of the evaluation
   * @param context
   */
  @Override
  public SEXP force(Context context) {
    return force(context, false);
  }

  @Override
  public SEXP force(Context context, boolean allowMissing) {
    if (result == null) {
      this.result = doEval(context, allowMissing);
    }
    return result;
  }

  protected SEXP doEval(Context context, boolean allowMissing) {
    return context.evaluate(expression, environment, allowMissing);
  }
  
  
  public void setResult(SEXP exp) {
    this.result = exp;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return this Promise's original expression.
   */
  public SEXP getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    if(result == null) {
      return "Unevaluated{" + expression + "}";
    } else {
      return "Evaluated{" + result + "}";
    }
  }

  public static Promise repromise(SEXP value) {
    return new Promise(value, value);
  }


  public Environment getEnvironment() {
    return environment;
  }
  
  /**
   * 
   * @return the evaluated value of the Promise, or {@code null}
   * if the Promise has not yet been evaluated.
   */
  public SEXP getValue() {
    return result;
  }
  
  public boolean isEvaluated() {
    return result != null;
  }

  public static Promise repromise(Environment environment, SEXP expression) {
    if(expression instanceof Promise) {
      return (Promise)expression;
    } else {
      return new Promise(environment, expression);
    }
  }

  @Override
  public final SEXP promise(Environment environment) {
    return this;
  }

  @Override
  public SEXP promise() {
    return this;
  }
}
