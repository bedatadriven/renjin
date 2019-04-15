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
import org.renjin.eval.DispatchTable;


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

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch) {
    PairList.Builder list = new PairList.Builder();
    for (int i = 0; i < argumentNames.length; i++) {
      list.add(argumentNames[i], promisedArguments[i]);
    }
    return apply(context, rho, call, list.build());
  }
}
