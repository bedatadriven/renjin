/**
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
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.sexp.*;

public class SwitchFunction extends SpecialFunction {

  public SwitchFunction() {
    super("switch");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    return doApply(context, rho, call, args);
  }

  private static SEXP doApply(Context context, Environment rho, FunctionCall call, PairList args) {

    ArgumentIterator argIt = new ArgumentIterator(context, rho, args);
    if(!argIt.hasNext()) {
      throw new EvalException("argument \"EXPR\" is missing");
    }

    PairList.Node exprNode = argIt.nextNode();
    if(exprNode.hasTag() && !exprNode.getTag().getPrintName().equals("EXPR")) {
      throw new EvalException("supplied argument name '%s' does not match 'EXPR'", exprNode.getTag().getPrintName());
    }

    SEXP expr = context.evaluate(exprNode.getValue(), rho);

    if(expr.length() == 1) {
      if (expr instanceof StringVector) {
        return matchByName(context, rho, expr, argIt);
      } else if (expr instanceof AtomicVector) {
        return matchByPosition(context, rho, expr, argIt);
      }
    }
    throw new EvalException("EXPR must be a length 1 vector");
  }

  private static SEXP matchByName(Context context, Environment rho, SEXP expr, ArgumentIterator argIt) {
    String name = expr.asString();
    if(StringVector.isNA(name)) {
      name = "NA";
    }

    while(argIt.hasNext()) {
      PairList.Node argNode = argIt.nextNode();

      // Match by name
      if(argNode.hasTag() && argNode.getTag().getPrintName().equals(name)) {

        // Skip to the next non-missing argument, to support constructions like:
        // switch("a", a = , b = , c = 32)
        while(argNode.getValue() == Symbol.MISSING_ARG && argIt.hasNext()) {
          argNode = argIt.nextNode();
        }

        // Evaluate and return the matching argument
        return context.evaluate(argNode.getValue(), rho);
      }

      // If there are no matches, match the last unnamed argument, if one is present.
      if(!argNode.hasTag() && !argIt.hasNext()) {
        return context.evaluate(argNode.getValue(), rho);
      }
    }

    // If there are no matches, and no final unnamed argument, the result is NULL
    return Null.INSTANCE;
  }

  private static SEXP matchByPosition(Context context, Environment rho, SEXP expr, ArgumentIterator argIt) {

    int pos = ((AtomicVector) expr).getElementAsInt(0);

    if(!IntVector.isNA(pos) && pos > 0) {
      int argIndex = 1;
      while (argIt.hasNext()) {
        PairList.Node argNode = argIt.nextNode();
        if (argIndex == pos) {
          return context.evaluate(argNode.getValue(), rho);
        }
        argIndex++;
      }
    }

    return Null.INSTANCE;
  }

  public static SEXP matchAndApply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] arguments) {
    PairList.Builder args = new PairList.Builder();
    for(int i =0;i!=arguments.length;++i) {
      args.add(argumentNames[i], arguments[i]);
    }
    return doApply(context, rho, call, args.build());
  }
}
