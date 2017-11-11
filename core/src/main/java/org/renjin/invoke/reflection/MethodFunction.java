/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.invoke.reflection;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

/**
 * An R Function object that wraps a JVM method. The object stores both the reference
 * to the method and an instance on which the function is to be applied.
 */
public class MethodFunction extends AbstractSEXP implements Function {

  private final String name;
  private final Object instance;
  private final FunctionBinding functionBinding;
  
  public MethodFunction(Object instance, FunctionBinding functionBinding) {
    super();
    this.instance = instance;
    this.functionBinding = functionBinding;
    this.name = functionBinding.getName();
  }

  public String getName() {
    return name;
  }

  @Override
  public SEXPType getType() {
    return SEXPType.BUILTIN;
  }

  @Override
  public String getTypeName() {
    return "method";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    return functionBinding.evaluateArgsAndInvoke(instance, context, rho, args);
  }

  /**
   *
   * @return  the JVM class instance to which the function is bound, or {@code null} if the
   * method is static
   */
  public Object getInstance() {
    return instance;
  }

  /**
   *
   * @return  true if this method is static and has no instance binding
   */
  public boolean isStatic() {
    return instance == null;
  }

  /*
   * @return
   */
  public FunctionBinding getFunctionBinding() {
    return functionBinding;
  }
  
  @Override
  public String toString() {
    return functionBinding.getDeclaringClass().getName() + ":" + functionBinding.toString();
  }

}
