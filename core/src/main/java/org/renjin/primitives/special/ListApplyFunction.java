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
import org.renjin.sexp.*;

public class ListApplyFunction extends ApplyFunction {

  private static final ArgumentMatcher MATCHER = new ArgumentMatcher("X", "FUN", "...");

  public ListApplyFunction() {
    super("lapply");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    MatchedArguments matched = MATCHER.expandAndMatch(context, rho, args);
    SEXP vector = context.evaluate(matched.getActualForFormal(0), rho);
    SEXP function = matchFunction(context, rho, matched.getActualForFormal(1));
    PairList extraArguments = promiseExtraArguments(rho, matched);

    return applyList(context, rho, vector, function, extraArguments)
        .setAttribute(Symbols.NAMES, vector.getAttributes().getNamesOrNull())
        .build();
  }
}
