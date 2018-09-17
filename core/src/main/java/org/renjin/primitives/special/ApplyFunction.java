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
import org.renjin.eval.MatchedArguments;
import org.renjin.primitives.Types;
import org.renjin.sexp.*;

public abstract class ApplyFunction extends SpecialFunction {

  public ApplyFunction(String name) {
    super(name);
  }

  protected final PairList promiseExtraArguments(Environment rho, MatchedArguments matched) {
    PairList.Builder extra = new PairList.Builder();
    for (int i = 0; i < matched.getActualCount(); i++) {
      if(matched.isExtraArgument(i)) {
        extra.add(matched.getActualTag(i), Promise.repromise(rho, matched.getActualValue(i)));
      }
    }
    return extra.build();
  }

  protected ListVector applyList(Context context, Environment rho, SEXP vector, SEXP function, PairList extraArguments) {

    if(!Types.isVector(vector, "any") || Types.isObject(vector)) {
      FunctionCall asListCall = FunctionCall.newCall(Symbol.get("as.list"), Promise.repromise(vector));
      vector = context.evaluate(asListCall, rho);
    }

    ListVector.Builder builder = ListVector.newBuilder();
    builder.setAttribute(Symbols.NAMES, vector.getAttributes().getNamesOrNull());

    for(int i=0;i!=vector.length();++i) {
      // For historical reasons, the calls created by lapply are unevaluated, and code has
      // been written (e.g. bquote) that relies on this.
      FunctionCall getElementCall = FunctionCall.newCall(Symbol.get("[["), vector, new IntArrayVector(i+1));
      FunctionCall applyFunctionCall = new FunctionCall(function, new PairList.Node(getElementCall, extraArguments));
      builder.add( context.evaluate(applyFunctionCall, rho) );
    }
    return builder.build();
  }

  protected final Function matchFunction(Context context, Environment rho, SEXP functionArgument) {
    functionArgument = functionArgument.force(context);
    SEXP evaluatedArgument = context.evaluate(functionArgument, rho).force(context);

    if (evaluatedArgument instanceof Function) {
      return (Function) evaluatedArgument;
    }

    Symbol name;
    if (evaluatedArgument instanceof Symbol) {
      name = (Symbol) evaluatedArgument;

    } else if (evaluatedArgument instanceof StringVector && evaluatedArgument.length() == 1) {
      name = Symbol.get(((StringVector) evaluatedArgument).getElementAsString(0));

    } else if(functionArgument instanceof Symbol) {

      // Fallback to the unevaluated name if the evaluated argument is not a function or a character
      // string

      name = (Symbol) functionArgument;

    } else {
      throw new EvalException("'%s' is not a function, character or symbol", evaluatedArgument.toString());
    }

    Function function = rho.findFunction(context, name);
    if (function == null) {
      throw new EvalException("Function '" + name + " not found");
    }

    return function;
  }
}
