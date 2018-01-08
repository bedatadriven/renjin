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
import org.renjin.primitives.S3;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.sexp.*;

public class DollarAssignFunction extends SpecialFunction {


  public DollarAssignFunction() {
    super("$<-");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    // Even though this function is generic, it MUST be called with exactly three arguments
    if(args.length() != 3) {
      throw new EvalException(String.format("%d argument(s) passed to '$<-' which requires 3", args.length()));
    }

    SEXP object = context.evaluate(args.getElementAsSEXP(0), rho);
    StringVector nameArgument = DollarFunction.evaluateName(args.getElementAsSEXP(1));
    SEXP value = context.evaluate(args.getElementAsSEXP(2), rho);

    // For possible generic dispatch, repackage the name argument as character vector rather than
    // symbol
    PairList.Node repackagedArgs = new PairList.Node(object,
        new PairList.Node(nameArgument,
            new PairList.Node(Symbol.get("value"), value,
                Null.INSTANCE)));

    SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, "$<-", object, repackagedArgs);
    if (genericResult!= null) {
      return genericResult;
    }

    // If no generic function, replace the element
    String name = nameArgument.getElementAsString(0);
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
