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

import org.renjin.primitives.CollectionUtils;
import org.renjin.primitives.special.ReturnException;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Collections2;
import org.renjin.repackaged.guava.collect.Iterators;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.PeekingIterator;
import org.renjin.sexp.*;

import java.util.*;

import static org.renjin.repackaged.guava.collect.Collections2.filter;
import static org.renjin.repackaged.guava.collect.Collections2.transform;


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
    return apply(callingContext, callingEnvironment, call, closure, promisedArgs, Collections.<Symbol, SEXP>emptyMap());
  }

  public static SEXP apply(Context callingContext, Environment callingEnvironment,
                     FunctionCall call, Closure closure, PairList promisedArgs, Map<Symbol, SEXP> metadata) {

    Context functionContext = callingContext.beginFunction(callingEnvironment, call, closure, promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();

    try {
      matchArgumentsInto(closure.getFormals(), promisedArgs, functionContext, functionEnvironment);

      if(!metadata.isEmpty()) {
        for (Map.Entry<Symbol, SEXP> entry : metadata.entrySet()) {
          functionEnvironment.setVariableUnsafe(entry.getKey(), entry.getValue());
        }
      }

      return closure.doApply(functionContext);

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

    } catch(EvalException e) {

      e.initContext(functionContext);
      SEXP handler = findHandler(functionContext, Arrays.asList("simpleError", "error", "condition"));
      if(handler != null) {
        // the R code in conditions.R expects this format (condition, message, handler).
        // I think is the kind of thing that should be moved entirely into java to avoid
        // these complicated relationships between R and Java/C code but i don't want
        // to mess with the R code too much at this point.

        return new ListVector(e.getCondition(), Null.INSTANCE, handler);
      } else {
        throw e;
      }
    } finally {
      functionContext.exit();
    }
  }
  
  private static SEXP findHandler(Context context, Iterable<String> conditionClasses) {
    for(String conditionClass : conditionClasses) {
      ConditionHandler handler = context.getConditionHandler(conditionClass);
      if(handler != null) {
        return handler.getFunction();
      }
    }
    return null;
  }
  
  public static void matchArgumentsInto(PairList formals, PairList actuals, 
      Context innerContext, Environment innerEnv) {

    PairList matched = matchArguments(formals, actuals);
    for(PairList.Node node : matched.nodes()) {
      SEXP value = node.getValue();
      if(value == Symbol.MISSING_ARG) {
        SEXP defaultValue = formals.findByTag(node.getTag());
        if(defaultValue != Symbol.MISSING_ARG) {
          value =  Promise.promiseMissing(innerEnv, defaultValue);
        }
      }
      innerEnv.setVariable(innerContext, node.getTag(), value);
    }
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

    int numActuals = actuals.length();
    SEXP actualTags[] = new SEXP[numActuals];
    String actualNames[] = new String[numActuals];
    SEXP actualValues[] = new SEXP[numActuals];
    {
      int i = 0;
      for (PairList.Node node : actuals.nodes()) {
        actualTags[i] = node.getRawTag();
        if (node.hasName()) {
          actualNames[i] = node.getName();
        }
        actualValues[i] = node.getValue();
        i++;
      }
    }
    ArgumentMatcher matcher = new ArgumentMatcher(formals);
    MatchedArguments matching = matcher.match(actualNames);

    PairList.Builder result = new PairList.Builder();
    for(int formalIndex = 0; formalIndex < matching.getFormalCount(); ++formalIndex) {

      if(matcher.isFormalElipses(formalIndex)) {
        PromisePairList.Builder promises = new PromisePairList.Builder();
        for (int actualIndex = 0; actualIndex < numActuals; actualIndex++) {
          if(matching.isExtraArgument(actualIndex)) {
            promises.add( actualTags[actualIndex],  actualValues[actualIndex] );
          }
        }
        result.add(matching.getFormal(formalIndex), promises.build());

      } else {
        int actualIndex = matching.getActualIndex(formalIndex);
        if(actualIndex == -1) {
          if(populateMissing) {
            result.add(matching.getFormal(formalIndex), Symbol.MISSING_ARG);
          }
        } else {
          result.add(matching.getFormal(formalIndex), actualValues[actualIndex]);
        }
      }
    }

    return result.build();
  }

}
