/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

/**
 * Provides the implementation of the `function` function, which creates
 * a closure from a pair list of formals and an unevaluated body.
 */
public class ClosureFunction extends SpecialFunction {

  public ClosureFunction() {
    super("function");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    return apply(rho, args);
  }

  public static Closure apply(Environment rho, PairList args) {
    if(args.length() < 2) {
      throw new EvalException("incorrect number of arguments to \"function\"");
    }
    SEXP formals = args.getElementAsSEXP(0);
    if(!(formals instanceof PairList) || formals instanceof FunctionCall) {
      throw new EvalException("invalid formal argument list for \"function\"");
    }

    SEXP body = args.getElementAsSEXP(1);

    return new Closure(rho, (PairList) formals, body);
  }
}
