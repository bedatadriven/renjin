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
package org.renjin.sexp;

import org.renjin.eval.Context;


public abstract class BuiltinFunction extends PrimitiveFunction {

  public static final String TYPE_NAME = "builtin";
  public static final String IMPLICIT_CLASS = "function";
  
  private final String name;
  
  public BuiltinFunction(String name) {
    this.name = name;
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public final String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  @Override
  public final void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Applies this {@code BuiltinFunction} to the given the
   * @param context the runtime context in which to evaluate this function
   * @param call the original function call
   * @param argumentNames the names of the arguments
   * @param arguments the <b><i>evaluated</i></b> arguments
   * @return the result of the function
   */
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames,
      SEXP arguments[]) {
    throw new UnsupportedOperationException(getName());
  }
}
