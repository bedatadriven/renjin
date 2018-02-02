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
import org.renjin.eval.MatchedArguments;
import org.renjin.primitives.S3;
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

    // Match the provided arguments to the formals of the generic

    MatchedArguments matchedArguments = matcher.match(expandedArgs);

    // Given the complete list of arguments, we need two things:
    // 1) A list of the classes of the evaluated arguments
    // 2) A pairlist of promises that include the original expression _and_ the evaluated
    //    expression which can ultimately be passed to the selected method and avoid double evaluation.

    PairList.Builder promisedArgs = new PairList.Builder();

    for (int formalIndex = 0; formalIndex < matchedArguments.getFormalCount(); formalIndex++) {

      Symbol formalName = matchedArguments.getFormalName(formalIndex);
      int actualIndex = matchedArguments.getActualIndex(formalIndex);

      if(actualIndex == -1) {
        // This formal argument was not provided by the caller
        promisedArgs.add(formalName, Symbol.MISSING_ARG);

      } else {
        SEXP uneval = matchedArguments.getActualValue(actualIndex);
        if(actualIndex == 0) {
          // The source has already been evaluated to check for class
          promisedArgs.add(formalName, new Promise(uneval, object));

        } else {
          promisedArgs.add(formalName, Promise.repromise(rho, uneval));
        }
      }
    }
    this.promisedArgs = promisedArgs.build();
  }

  public PairList getPromisedArgs() {
    return promisedArgs;
  }

  public Signature getSignature(int length) {
    String[] classes = new String[length];
    int index = 0;
    for (PairList.Node actual : promisedArgs.nodes()) {
      SEXP evaluated = actual.force(context);
      if(evaluated == Symbol.MISSING_ARG) {
        classes[index] = "missing";
      } else {
        classes[index] = computeDateClass(evaluated);
      }
      index++;
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
