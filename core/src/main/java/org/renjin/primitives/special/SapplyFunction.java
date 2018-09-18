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

import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.MatchedArguments;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.primitives.Identical;
import org.renjin.sexp.*;

public class SapplyFunction extends ApplyFunction {

  public static final ArgumentMatcher MATCHER = new ArgumentMatcher("X", "FUN", "...", "simplify", "USE.NAMES");

  public SapplyFunction() {
    super("sapply");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    MatchedArguments matched = MATCHER.match(args);
    SEXP vector = context.evaluate(matched.getActualForFormal(0), rho);
    SEXP functionArgument = matched.getActualForFormal(1);
    Function function = matchFunction(context, rho, functionArgument);
    SEXP simplifyArgument = context.evaluate(matched.getActualForFormal(3, LogicalVector.TRUE));
    boolean simplify = !(Identical.identical(simplifyArgument, LogicalVector.FALSE));
    boolean useNames = WrapperRuntime.convertToBooleanPrimitive(
        context.evaluate(matched.getActualForFormal(4, LogicalVector.TRUE)));

    PairList extraArguments = promiseExtraArguments(rho, matched);

    if(vector.length() >= 120 && vector instanceof Vector && extraArguments == Null.INSTANCE)  {
      SEXP result = tryCompileAndEval(context, rho, call, (Vector) vector, functionArgument, function, simplify);
      if(result != null) {
        return result;
      }
    }


    ListVector list = applyList(context, rho, vector, function, extraArguments);

    Vector result;
    if(simplify) {
      result = simplifyToArray(list, true);
    } else {
      result = list;
    }

    if(useNames && vector instanceof StringVector && !result.getAttributes().hasNames()) {
      result = (Vector) result.setAttribute(Symbols.NAMES, vector);
    }

    return result;
  }

}
