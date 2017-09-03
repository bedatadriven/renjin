/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.eval;

import org.renjin.primitives.special.ReturnException;
import org.renjin.sexp.*;

/**
 * Routines for dispatching and generally organizing function calls.
 * Much of this code is a pretty literal port of portions of eval.c and
 * object.c
 */
public class Calls {


  public static SEXP applyClosure(Closure closure, Context context, Environment callingEnvironment, FunctionCall call, PairList promisedArgs,
                                  Frame suppliedEnvironment) {

    Context functionContext = context.beginFunction(callingEnvironment, call, closure, promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();

    try {
      ClosureDispatcher.matchArgumentsInto(closure.getFormals(), promisedArgs, functionEnvironment);

      // copy supplied environment values into the function environment
      for(Symbol name : suppliedEnvironment.getSymbols()) {
        // functionEnvironment is just created and has no bindings yet, therefore we use the unsafe version of setVariable
        functionEnvironment.setVariableUnsafe(name, suppliedEnvironment.getVariable(name));
      }

      return functionContext.evaluate( closure.getBody(), functionEnvironment);

    } catch(ReturnException e) {

      if(e.getEnvironment() != functionEnvironment) {
        throw e;
      }
      return e.getValue();

    } finally {
      functionContext.exit();
    }
  }

  /**
   *  Create a list of promises from a list of unevaluated arguments.
   *
   *  @param argumentList a pairlist of arguments passed to the function call
   *  @param context the current evaluation context
   *  @param rho the environment in which the arguments should be evaluated
   */
  public static PairList promiseArgs(PairList argumentList, Context context, Environment rho) {
    PairList.Builder list = new PairList.Builder();

    for(PairList.Node node : argumentList.nodes()) {

      /* If we have a ... symbol, we look to see what it is bound to.
      * If its binding is Null (i.e. zero length)
      * we just ignore it and return the cdr with all its
      * expressions promised; if it is bound to a ... list
      * of promises, we repromise all the promises and then splice
      * the list of resulting values into the return value.
      * Anything else bound to a ... symbol is an error
      */

      if (node.getValue().equals(Symbols.ELLIPSES)) {

        PromisePairList dotExp = (PromisePairList)rho.findVariable(context, Symbols.ELLIPSES);
        for(PairList.Node dotNode : dotExp.nodes()) {
          list.add(dotNode.getRawTag(), dotNode.getValue());
        }

      } else if (node.getValue() == Symbol.MISSING_ARG) {
        list.add(node.getRawTag(), node.getValue());

      } else {
        list.add(node.getRawTag(), Promise.repromise(rho, node.getValue()));
      }
    }
    return list.build();
  }
}
