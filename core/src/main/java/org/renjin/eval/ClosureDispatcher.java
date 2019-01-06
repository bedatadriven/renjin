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
package org.renjin.eval;

import org.renjin.primitives.special.ReturnException;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.Map;


public class ClosureDispatcher {

  private final FunctionCall call;
  private final Environment callingEnvironment;
  private final Context callingContext;

  private DispatchChain dispatchChain;

  public ClosureDispatcher(Context callingContext, Environment callingEnvironment, FunctionCall call) {
    this.call = call;
    this.callingEnvironment = callingEnvironment;
    this.callingContext = callingContext;
  }


  public SEXP apply(DispatchChain chain, PairList arguments) {
    this.dispatchChain = chain;
    return apply(callingContext, callingEnvironment, call, chain.getClosure(), arguments, dispatchChain.createMetadata());
  }

  public SEXP applyClosure(Closure closure, PairList args) {
    PairList promisedArgs = Calls.promiseArgs(args, callingContext, callingEnvironment);
    return apply(callingContext, callingEnvironment, call, closure, promisedArgs, Collections.emptyMap());
  }

  /**
   * Evaluates a function call to an R-language closure.
   *
   * @param callingContext the evaluation context in which this closure is to be applied
   * @param callingEnvironment the environment in which this takes place
   * @param call the FunctionCall to evaluate
   * @param closure the resolved closure
   * @param promisedArgs a pairlist of the arguments provided to the call, each promised in the calling environment
   * @param metadata additional bindings to be added to the new environment created for the function call.
   * @return the result of the function call to the {@code closure}
   */
  public static SEXP apply(Context callingContext, Environment callingEnvironment,
                     FunctionCall call, Closure closure, PairList promisedArgs, Map<Symbol, SEXP> metadata) {

    Context functionContext = callingContext.beginFunction(callingEnvironment, call, closure, promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();

    try {
      matchArgumentsInto(closure.getFormals(), promisedArgs, functionEnvironment);

      if(!metadata.isEmpty()) {
        for (Map.Entry<Symbol, SEXP> entry : metadata.entrySet()) {
          functionEnvironment.setVariableUnsafe(entry.getKey(), entry.getValue());
        }
      }

      try {
        return closure.doApply(functionContext);
      } catch (EvalException e) {
        // Associate this EvalException with this function call context if it's not already.
        // N.B. initContext() also searches for condition handlers and may rethrow this
        // EvalException as a ConditionException if found.
        e.initContext(functionContext);
        throw e;
      }

    } catch(ReturnException e) {
      if (e.getEnvironment() != functionEnvironment) {
        throw e;
      }
      return e.getValue();

    } catch(ConditionException e) {
      if (e.getHandlerContext() == functionContext) {
        return new ListVector(e.getCondition(), Null.INSTANCE, e.getHandler());
      } else {
        throw e;
      }

    } catch (RestartException e) {
      if(e.getExitEnvironment() == functionContext.getEnvironment()) {
        // This return value is consumed by the R code in conditions.R
        return e.getArguments();
      } else {
        throw e;
      }

    } finally {
      functionContext.exit();
    }
  }


  /**
   * Matches the {@code actual} arguments provided to the function call to the Closure's formal argument list and
   * populates the {@code functionEnv} with the matched symbols.
   *
   *
   * @param formals the formal arguments of the closure. This should be a pairlist in the form
 *            {@code (a = <missing>, b = <default value>, ...)}
   * @param actuals the actual arguments provided to the function call, promised in the {@code callingEnvironment}
   * @param functionEnv the environment of the function call.
   */
  public static void matchArgumentsInto(PairList formals, PairList actuals, Environment functionEnv) {

    ArgumentMatcher matcher = new ArgumentMatcher(formals);
    MatchedArguments matching = matcher.match(actuals);

    for (int formalIndex = 0; formalIndex < matching.getFormalCount(); formalIndex++) {
      if(matching.isFormalEllipses(formalIndex)) {
        functionEnv.setVariableUnsafe(Symbols.ELLIPSES, matching.buildExtraArgumentList());

      } else {
        Symbol formalName = matching.getFormalName(formalIndex);
        int actualIndex = matching.getActualIndex(formalIndex);

        if(actualIndex == -1) {

          // IF: No argument has been matched to this formal.
          // Use the default value for the formal.
          // For example, for the closure function(a, b = a * 2)
          // the default value of 'b' is 'a*2'.

          SEXP defaultValue = matcher.getDefaultValue(formalIndex);

          functionEnv.setMissingArgument(formalName,
              promiseDefaultValue(functionEnv, defaultValue));

        } else {

          // ELSE: an argument has been provided.

          SEXP actualValue = matching.getActualValue(actualIndex);
          if(actualValue == Symbol.MISSING_ARG) {

            // BUT, the provided argument may STILL be missing if the argument was
            // explicitly omitted. For example, in the function calls
            // f( , 3, 4) and x[, 2] the first argument has been ommitted and will
            // have the value of Symbol.MISSING_ARG

            SEXP defaultValue = matcher.getDefaultValue(formalIndex);

            functionEnv.setMissingArgument(formalName,
                promiseDefaultValue(functionEnv, defaultValue));
          } else {

            functionEnv.setArgument(formalName, actualValue);
          }
        }
      }
    }
  }

  private static SEXP promiseDefaultValue(Environment functionEnv, SEXP defaultValue) {
    if(defaultValue != Symbol.MISSING_ARG) {
      // If a default value is provided, wrap it a promise within the *function's* environment so
      // that will be properly evaluated if used.
      defaultValue = Promise.repromise(functionEnv, defaultValue);
    }
    return defaultValue;
  }

  public static PairList matchArguments(PairList formals, PairList actuals) {
    return matchArguments(formals, actuals, true);
  }

  /**
   * Matches arguments to actuals
   * @param formals
   * @param actuals
   * @param populateMissing
   * @return
   */
  public static PairList matchArguments(PairList formals, PairList actuals, boolean populateMissing) {

    ArgumentMatcher matcher = new ArgumentMatcher(formals);
    MatchedArguments matching = matcher.match(actuals);

    PairList.Builder result = new PairList.Builder();
    for(int formalIndex = 0; formalIndex < matching.getFormalCount(); ++formalIndex) {

      if(matching.isFormalEllipses(formalIndex)) {
        result.add(Symbols.ELLIPSES, matching.buildExtraArgumentList());

      } else {
        int actualIndex = matching.getActualIndex(formalIndex);
        if(actualIndex == -1) {
          if(populateMissing) {
            result.add(matching.getFormalName(formalIndex), Symbol.MISSING_ARG);
          }
        } else {
          result.add(matching.getFormalName(formalIndex), matching.getActualValue(actualIndex));
        }
      }
    }

    return result.build();
  }

}
