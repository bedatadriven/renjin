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
import org.renjin.primitives.S3;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.sexp.*;

public class DollarAssignFunction extends SpecialFunction {


  public DollarAssignFunction() {
    super("$<-");
  }


  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {

    // Even though this function is generic, it MUST be called with exactly three arguments
    checkArity(call, 3);

    SEXP object = context.evaluate(call.getArgument(0), rho);
    StringVector nameArgument = DollarFunction.evaluateName(call.getArgument(1));
    SEXP value = context.evaluate(call.getArgument(2), rho);


    return assign(context, rho, call, object, nameArgument.getElementAsString(0), value);
  }

  public static SEXP assign(Context context, Environment rho, FunctionCall call, SEXP object, String name, SEXP value) {
    if(object.isObject()) {
      // For possible generic dispatch, repackage the name argument as character vector rather than
      // symbol
      String[] argumentNames = new String[3];
      SEXP[] promisedArgs = new SEXP[3];
      promisedArgs[0] = object.repromise();
      promisedArgs[1] = StringVector.valueOf(name);
      promisedArgs[2] = value.repromise();

      SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, "$<-", null, argumentNames, promisedArgs);
      if (genericResult!= null) {
        return genericResult;
      }
    }

    // If no generic function, replace the element
    if(object instanceof PairList.Node) {
      return Subsetting.setElementByName((PairList.Node)object, name, value);

    } else if(object instanceof Environment) {
      return Subsetting.setElementByName(context, (Environment)object, name, value);

    } else if(object instanceof ListVector) {
      return Subsetting.setElementByName((ListVector)object, name, value);

    } else if(object instanceof ExternalPtr) {
      return Subsetting.setElementByName((ExternalPtr<?>) object, name, value);

    } else if(object instanceof AtomicVector) {
      return Subsetting.setElementByName((AtomicVector) object, name, value);

    } else if(object instanceof S4Object) {
      return Subsetting.setElementByName(context, ((S4Object) object), name, value);
    } else {
      throw new EvalException("object of type '%s' is not subsettable", object.getTypeName());
    }
  }
}
