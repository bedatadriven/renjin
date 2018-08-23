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
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.S3;
import org.renjin.primitives.Types;
import org.renjin.sexp.*;

/**
 * {@code $} operator
 * 
 * <p>Requires special handling of the symbol argument and generics</p>
 */
public class DollarFunction extends SpecialFunction {
  
  public DollarFunction() {
    super("$");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

   
    // Even though this function is generic, it MUST be called with exactly two arguments
    if(args.length() != 2) {
      throw new EvalException(String.format("%d argument(s) passed to '$' which requires 2", args.length()));
    }

    SEXP object = context.evaluate(args.getElementAsSEXP(0), rho);
    StringVector nameArgument = evaluateName(args.getElementAsSEXP(1));

    // For possible generic dispatch, repackage the name argument as character vector rather than
    // symbol
    PairList.Node repackagedArgs = new PairList.Node(object, new PairList.Node(nameArgument, Null.INSTANCE));
    
    SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, "$", object, repackagedArgs);
    if (genericResult!= null) {
      return genericResult;
    }

    // Unwrap any environments hidden inside an S4 object
    object = Types.unwrapS4Object(object);
    
    // If no generic function, extract the element
    String name = nameArgument.getElementAsString(0);

    return apply(context, object, name);
  }

  public static SEXP apply(Context context, SEXP object, String name) {
    if(object instanceof PairList) {
      return fromPairList((PairList) object, name);
    
    } else if(object instanceof Environment) {
      return fromEnvironment(context, (Environment)object, name);
      
    } else if(object instanceof ListVector) {
      return fromList((ListVector) object, name);

    } else if(object instanceof ExternalPtr) {
      return fromExternalPtr((ExternalPtr<?>) object, name);
    
    } else if(object instanceof AtomicVector) {
      throw new EvalException("$ operator is invalid for atomic vectors");
    
    } else {
      throw new EvalException("object of type '%s' is not subsettable", object.getTypeName());
    }
  }

  static StringVector evaluateName(SEXP name) {
    if(name instanceof Symbol) {
      return new StringArrayVector(((Symbol) name).getPrintName());
    } else if(name instanceof StringVector) {
      return (StringVector) name;
    } else {
      throw new EvalException("invalid subscript type '%s'", name.getTypeName());
    }
  }
  
  public static SEXP fromPairList(PairList list, String name) {
    SEXP match = null;
    int matchCount = 0;

    for (PairList.Node node : list.nodes()) {
      if (node.hasTag()) {
        if (node.getTag().getPrintName().startsWith(name)) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }
  
  public static SEXP fromEnvironment(@Current Context context, Environment env, String name) {
    SEXP value = env.getVariable(context, name);
    if (value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value.force(context);
  }

  public static SEXP fromExternalPtr(ExternalPtr<?> externalPtr, String name) {
    return externalPtr.getMember(Symbol.get(name));
  }

  public static SEXP fromList(ListVector list, String name) {
    SEXP match = null;
    int matchCount = 0;

    for (int i = 0; i != list.length(); ++i) {
      String elementName = list.getName(i);
      if (!StringVector.isNA(elementName)) {
        if (elementName.equals(name)) {
          return list.getElementAsSEXP(i);
        } else if (elementName.startsWith(name)) {
          match = list.get(i);
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }
  
}
