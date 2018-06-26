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
import org.renjin.methods.Methods;
import org.renjin.primitives.Attributes;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;


public class AssignSlotFunction extends SpecialFunction {

  public AssignSlotFunction() {
    super("@<-");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    if(args.length() != 3) {
      throw new EvalException("Expected three arguments");
    }
    SEXP object = context.evaluate(args.getElementAsSEXP(0), rho);
    if(object == Null.INSTANCE) {
      throw new EvalException("Cannot set slots on the NULL object");
    }

    String slotName;
    SEXP which = args.getElementAsSEXP(1);
    if(which instanceof Symbol) {
      slotName = ((Symbol) which).getPrintName();
    } else if(which instanceof StringVector) {
      slotName = ((StringVector) which).getElementAsString(0);
    } else {
      throw new EvalException("invalid type '%s' for slot name", which.getTypeName());
    }

    SEXP unevaluatedValue = args.getElementAsSEXP(2);

    // Try to dispatch to an override, but repackage the name as a character
    PairList repackagedArgs = PairList.Node.fromArray(object, new StringArrayVector(slotName), unevaluatedValue);

    SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, getName(), object, repackagedArgs);
    if(genericResult != null) {
      return genericResult;
    }

    // Nope, we're going to use the default version so we need to evaluate the value
    SEXP rhs = context.evaluate(args.getElementAsSEXP(2));

    // verify that the slot assignment is permitted
    StringVector valueClass = Attributes.getClass(rhs);
    SEXP objectClass = object.getS3Class();

    context.getSession()
        .getS4Cache()
        .getS4ClassCache()
        .lookupClass(context, objectClass.asString())
        .getSlot(context, slotName)
        .checkAssignment(context, valueClass.getElementAsString(0));


    // Good to go, make the assignment
    return Methods.R_set_slot(context, object, slotName, rhs);
  }

}
