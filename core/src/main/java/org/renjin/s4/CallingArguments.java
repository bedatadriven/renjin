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
package org.renjin.s4;

import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Calls;
import org.renjin.eval.Context;
import org.renjin.sexp.*;

import java.util.Iterator;

/**
 * Prepares the arguments to the call for matching.
 *
 */
public class CallingArguments {

  private final PairList promisedArgs;
  private Context context;

  private final PairList expandedArgs;

  public CallingArguments(Context context, Environment rho, SEXP object, PairList args, ArgumentMatcher matcher) {
    this.context = context;

    // expand ... in arguments or remove if empty
    expandedArgs = Calls.promiseArgs(args, context, rho);

    // Create list of promised arguments without names
    PairList.Builder promisedArgs = new PairList.Builder();

    boolean firstArgument = true;
    for (SEXP argument : expandedArgs.values()) {
      if(firstArgument) {
        promisedArgs.add(new Promise(((Promise) argument).getExpression(), object));
      } else {
        promisedArgs.add(Promise.repromise(rho, argument));
      }
      firstArgument = false;
    }

    this.promisedArgs = promisedArgs.build();
  }

  public PairList getPromisedArgs() {
    return promisedArgs;
  }

  public Signature getSignature(int length) {
    String[] classes = new String[length];
    Iterator<PairList.Node> argumentIt = promisedArgs.nodes().iterator();
    for(int index = 0; index < length; ++index) {
      if(argumentIt.hasNext()) {
        SEXP actual = argumentIt.next().getValue();
        SEXP evaluated = actual.force(context);
        if (evaluated == Symbol.MISSING_ARG) {
          classes[index] = "missing";
        } else {
          classes[index] = computeDateClass(evaluated);
        }
      } else {
        classes[index] = "missing";
      }
    }
    return new Signature(classes);
  }

  public PairList getExpandedArgs() {
    return expandedArgs;
  }

  private String computeDateClass(SEXP evaluated) {
    AtomicVector classAttribute = evaluated.getAttributes().getClassVector();
    if (classAttribute.length() > 0) {
      /*
       * S3 Class has been explicitly defined
       */
      return classAttribute.getElementAsString(0);

    } else {
      /*
       * Compute implicit class based on DIM attribute and type
       */
      Vector dim = evaluated.getAttributes().getDim();
      if (dim.length() == 2) {
        return "matrix";
      } else if (dim.length() > 0) {
        return "array";
      } else if (evaluated instanceof IntVector) {
        return "integer";
      } else if (evaluated instanceof DoubleVector) {
        return "numeric";
      } else {
        return evaluated.getImplicitClass();
      }
    }
  }
}
