/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
  private boolean missingArgument;

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
    if (result == null) {
      this.result = doEval(context);
    }
    return result;
  }

  protected SEXP doEval(Context context) {
    return context.evaluate(expression, environment);
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
    return "{" + expression + "=>" + result + "}";
  }

  public static Promise repromise(SEXP value) {
    return new Promise(value, value);
  }


  /**
   * 
   * @return true if this {@code Promise}'s value is the default value for a missing 
   * argument.
   */
  public boolean isMissingArgument() {
    return missingArgument;
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
  
  public static Promise promiseMissing(Environment environment, SEXP defaultValue) {
    Promise promise = new Promise(environment, defaultValue);
    promise.missingArgument = true;
    return promise;
  }
  
  public static Promise repromise(Environment environment, SEXP expression) {
    if(expression instanceof Promise) {
      return (Promise)expression;
    } else {
      return new Promise(environment, expression);
    }
  }
}
