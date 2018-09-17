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
import org.renjin.primitives.combine.Combine;
import org.renjin.sexp.*;

public class SapplyFunction extends ApplyFunction {

  private static final ArgumentMatcher MATCHER = new ArgumentMatcher("X", "FUN", "...", "simplify", "USE.NAMES");

  public SapplyFunction() {
    super("sapply");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    MatchedArguments matched = MATCHER.match(args);
    SEXP vector = context.evaluate(matched.getActualForFormal(0), rho);
    SEXP function = matchFunction(context, rho, matched.getActualForFormal(1));
    PairList extraArguments = promiseExtraArguments(rho, matched);
    SEXP simplifyArgument = context.evaluate(matched.getActualForFormal(3, LogicalVector.TRUE));
    boolean simplify = (Identical.identical(simplifyArgument, LogicalVector.TRUE));
    boolean useNames = WrapperRuntime.convertToBooleanPrimitive(
        context.evaluate(matched.getActualForFormal(4, LogicalVector.TRUE)));

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

  private static Vector simplifyToArray(ListVector list, boolean higher) {

    if(list.length() == 0) {
      return list;
    }

    int commonLength = commonLength(list);
    if(commonLength == -1) {
      return list;
    }

    if(commonLength == 1) {
      return (Vector) Combine.unlist(list, false, true);

    } else if(commonLength > 1) {
      throw new UnsupportedOperationException("TODO");
    } else {
      return list;
    }
  }

  private static int commonLength(ListVector list) {
    int i = 0;
    int length = list.getElementAsSEXP(i).length();
    for(i = 1; i < list.length(); ++i) {
      int elementLength = list.getElementAsSEXP(i).length();
      if (elementLength != length) {
        return -1;
      }
    }
    return length;
  }
}
