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

import org.renjin.compiler.CachedApplyCall;
import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.MatchedArguments;
import org.renjin.sexp.*;

public class ListApplyFunction extends ApplyFunction {

  public static final ArgumentMatcher MATCHER = new ArgumentMatcher("X", "FUN", "...");

  public ListApplyFunction() {
    super("lapply");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    MatchedArguments matched = MATCHER.expandAndMatch(context, rho, args);
    SEXP vectorSymbol = matched.getActualForFormal(0);
    SEXP vector = context.evaluate(vectorSymbol, rho);
    SEXP functionArgument = matched.getActualForFormal(1);
    Function function = matchFunction(context, rho, functionArgument);
    PairList extraArguments = promiseExtraArguments(rho, matched);

    if(extraArguments == Null.INSTANCE && vector instanceof Vector &&
        (vector.length() >= 50 || call.cache instanceof CachedApplyCall))  {
      System.out.println("lapply(" + vectorSymbol + ")");
      SEXP result = tryCompileAndEval(context, rho, call, (Vector) vector, functionArgument, function, false);
      if(result != null) {
        return result;
      }
    }

    return applyList(context, rho, vector, function, extraArguments);
  }

}
