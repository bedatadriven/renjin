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
package org.renjin.primitives;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.*;

import java.io.IOException;

public class Args {

  
  public static SEXP args(@Current Context context, @Current Environment rho, SEXP function) throws IOException {

    if(function instanceof StringVector && function.length() == 1) {
      StringVector nameVector = (StringVector) function;
      String name = nameVector.getElementAsString(0);
      function = rho.findFunction(context, Symbol.get(name));
      if(function == null) {
        throw new EvalException("could not find function '" + name + "'");
      }
    }

    if(function instanceof Closure) {
      Closure closure = (Closure) function;
      return new Closure(context.getGlobalEnvironment(), closure.getFormals(), Null.INSTANCE);
    }

    if(function instanceof PrimitiveFunction) {
      PrimitiveFunction primitive = (PrimitiveFunction) function;
      Closure definition = findPrimitiveDefinition(context, primitive);
      if(definition != null) {
        return definition;
      }
    }

    return Null.INSTANCE;
  }

  public static Closure findPrimitiveDefinition(Context context, PrimitiveFunction primitive) {

    Environment argsEnv = (Environment) context
        .getBaseEnvironment()
        .getVariableUnsafe(".ArgsEnv")
        .force(context);

    SEXP definition = argsEnv.getVariableUnsafe(primitive.getName());
    if(definition instanceof Closure) {
      return (Closure) definition;
    }

    argsEnv = (Environment) context
        .getBaseEnvironment()
        .getVariableUnsafe(".GenericArgsEnv")
        .force(context);

    definition = argsEnv.getVariableUnsafe(primitive.getName());
    if(definition instanceof Closure) {
      return (Closure) definition;
    }
    return null;
  }
}
