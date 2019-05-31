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
import org.renjin.eval.EvalException;


public abstract class SpecialFunction extends PrimitiveFunction {
  
  public static final String TYPE_NAME = "special";

  private final String name;
  
  protected SpecialFunction(String name) {
    this.name = name;
  }
  
  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  public final String getName() {
    return name;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visitSpecial(this);
  }

  @Override
  public final SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch) {

    // Discard promised arguments, apply non-standard evaluation
    return apply(context, rho, call);

  }

  public abstract SEXP apply(Context context, Environment rho, FunctionCall call);


  public static boolean asLogicalNoNA(Context context, FunctionCall call, SEXP s) {

    if (s.length() == 0) {
      throw new EvalException("argument is of length zero");
    }
    if (s.length() > 1) {
      context.warn(call, "the condition has length > 1 and only the first element will be used");
    }

    s = context.materialize(s);
    
    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;
  }

  protected final void checkArity(FunctionCall call, int expectedArguments, int optional) {
    int count = call.getArguments().length();
    EvalException.check(count <= expectedArguments &&
            count >= (expectedArguments-optional),
        "invalid number of arguments");
  }

  protected final void checkArity(FunctionCall call, int expectedArguments) {
    int count = call.getArguments().length();
    EvalException.check(count == expectedArguments, "invalid number of arguments");
  }
}
