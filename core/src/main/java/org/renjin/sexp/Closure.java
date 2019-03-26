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

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;

import java.util.Objects;


/**
 * The function closure data type.
 *
 * <p>
 * In R functions are objects and can be manipulated in much the same way as any other object.
 * Functions (or more precisely, function closures) have three basic components:
 *  a formal argument list, a body and an environment.
 *
 */
public class Closure extends AbstractSEXP implements Function {

  public static final String TYPE_NAME = "closure";
  private Environment enclosingEnvironment;
  private SEXP body;
  private PairList formals;

  public Closure(Environment enclosingEnvironment, PairList formals, SEXP body, AttributeMap attributes) {
    super(attributes);
    assert !(formals instanceof FunctionCall);
    this.enclosingEnvironment = enclosingEnvironment;
    this.body = body;
    this.formals = formals; 
  }
 
  public Closure(Environment environment, PairList formals, SEXP body) {
    this(environment, formals, body, AttributeMap.EMPTY);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }
  

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap newAttributes) {
    return new Closure(this.enclosingEnvironment, this.formals, this.body, newAttributes);
  }

  @Override
  public String getImplicitClass() {
    return Function.IMPLICIT_CLASS;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    
    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, call);
    return dispatcher.applyClosure(this, args);
  }


  public SEXP doApply(Context functionContext) {
    return functionContext.evaluate(body);
  }
   

  /**
   * A function's <strong> evaluation environment</strong> is the environment
   * that was active at the time that the
   * function was created. Any symbols bound in that environment are
   * captured and available to the function. This combination of the code of the
   * function and the bindings in its environment is called a `function closure', a
   * term from functional programming theory.
   *
   */
  public Environment getEnclosingEnvironment() {
    return enclosingEnvironment;
  }

  /**
   * Creates a copy of this Closure with the new enclosing environment.
   * @param env the new enclosing environment.
   * @return
   */
  public Closure setEnclosingEnvironment(Environment env) {
    return new Closure(env, formals, body, getAttributes());
  }

  /**
   * The body is a parsed R statement.
   * It is usually a collection of statements in braces but it
   * can be a single statement, a symbol or even a constant.
   */
  public SEXP getBody() {
    return body;
  }

  /**
   * The formal argument list is a a pair list of arguments.
   * An argument can be a symbol, or a ‘symbol = default’ construct, or
   * the special argument ‘...’.
   *
   * <p> The second form of argument is
   *  used to specify a default value for an argument.
   * This value will be used if the function is called
   *  without any value specified for that argument.
   * The ‘...’ argument is special and can contain any number of arguments.
   * It is generally used if the number of arguments
   * is unknown or in cases where the arguments will
   * be passed on to another function.
   */
  public PairList getFormals() {
    return formals;
  }

  public void unsafeSetFormals(PairList formals) {
    this.formals = formals;
  }

  public void unsafeSetBody(SEXP body) {
    this.body = body;
  }

  public void unsafeSetEnclosingEnvironment(Environment v) {
    this.enclosingEnvironment = v;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("function(");
    if(getFormals() instanceof PairList.Node) {
      ((PairList.Node) getFormals()).appendValuesTo(sb);
    }
    return sb.append(")").toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime
        * result
        + ((enclosingEnvironment == null) ? 0 : enclosingEnvironment.hashCode());
    result = prime * result + ((formals == null) ? 0 : formals.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Closure)) {
      return false;
    }
    Closure other = (Closure) obj;
    if(!Objects.equals(body, other.body)) {
      return false;
    }
    if(!Objects.equals(enclosingEnvironment, other.enclosingEnvironment)) {
      return false;
    }
    if(!Objects.equals(formals, other.formals)) {
      return false;
    }
    return true;
  }
}
