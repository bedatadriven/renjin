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

package r.lang;

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

  public static final int TYPE_CODE = 5;
  public static final String TYPE_NAME = "promise";

  private Context context;
  private Environment environment;
  private SEXP expression;
  private EvalResult result;

  public Promise(Context context, Environment environment, SEXP expression) {
    this.expression = expression;
    this.environment = environment;
    this.context = context;
  }

  /**
   * Creates a promise with it's expression and its already-evaluated
   * result.
   * @param expression
   * @param result
   */
  public Promise(SEXP expression, SEXP result) {
    this.environment = null;
    this.context = null;
    this.expression = expression;
    this.result = new EvalResult(result, true);
  }

  @Override
  public EvalResult evaluate(Context context, Environment rho) {
    return force();
  }

  /**
   * Forces the evaluation of this Promise, if it has not already
   * been evaluated.
   *
   * @return the result of the evaluation
   */
  public EvalResult force() {
    if (result == null) {
      this.result = expression.evaluate(this.context, environment);
      this.environment = null;
      this.context = null;
    }
    return result;
  }

  public void setResult(SEXP exp) {
    this.result = new EvalResult(exp);
    this.environment = null;
    this.context = null;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
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
    return expression.toString();
  }
}
