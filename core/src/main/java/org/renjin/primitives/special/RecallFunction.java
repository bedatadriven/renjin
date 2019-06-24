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
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class RecallFunction extends SpecialFunction {

  public RecallFunction() {
    super("Recall");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {

    // this is an .Internal function, so we need to go up one context.
    if (!(context.getFunction() instanceof Closure)) {
      throw new EvalException("Recall() must be called from within a closure");
    }

    Closure closure = (Closure)context.getFunction();

    FunctionCall newCall = new FunctionCall(context.getCall().getFunction(), call.getArguments());
    
    return closure.apply(context,
          context.getEnvironment(),
          newCall);
  }

  
  
}
