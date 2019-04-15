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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

public class InternalFunction extends SpecialFunction {

  public InternalFunction() {
    super(".Internal");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch) {
    Promise internalCallPromise = (Promise) promisedArguments[0];
    FunctionCall internalCall = (FunctionCall) internalCallPromise.getExpression();
    Symbol internalName = (Symbol)internalCall.getFunction();
    Function function = Primitives.getInternal(internalName);

    // Evaluate arguments to the internal call
    List<String> names = new ArrayList<>();
    List<SEXP> values = new ArrayList<>();
    for (PairList.Node node : internalCall.getArguments().nodes()) {

      if(node.getValue() == Symbols.ELLIPSES) {
        PromisePairList expando = (PromisePairList) rho.getEllipsesVariable();
        for (PairList.Node expandoNode : expando.nodes()) {
          names.add(expandoNode.hasTag() ? expandoNode.getName() : null);
          values.add(expandoNode.getValue());
        }
      } else {
        names.add(node.hasTag() ? node.getName() : null);
        values.add(Promise.repromise(rho, node.getValue()));
      }
    }
    return function.apply(context, rho, internalCall, names.toArray(new String[0]), values.toArray(new SEXP[0]), null);
  }
}
