/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.eval;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import org.renjin.primitives.CollectionUtils;
import org.renjin.primitives.special.ReturnException;
import org.renjin.sexp.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

/**
 * Routines for dispatching and generally organizing function calls.
 * Much of this code is a pretty literal port of portions of eval.c and
 * object.c
 */
public class Calls {

  public static PairList evaluateList(Context context, Environment rho, PairList args) {
    PairList.Builder evaled = new PairList.Builder();
    for(PairList.Node node : args.nodes()) {
      evaled.add(node.getRawTag(), context.evaluate( node.getValue(), rho));
    }
    return evaled.build();
  }


  public static SEXP applyClosure(Closure closure, Context context, Environment callingEnvironment, FunctionCall call, PairList promisedArgs, Environment rho,
                                        Frame suppliedEnvironment) {

    Context functionContext = context.beginFunction(callingEnvironment, call, closure, promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();

    try {
      matchArgumentsInto(closure.getFormals(), promisedArgs, functionContext, functionEnvironment);

      // copy supplied environment values into the function environment
      for(Symbol name : suppliedEnvironment.getSymbols()) {
        functionEnvironment.setVariable(name, suppliedEnvironment.getVariable(name));
      }

      SEXP result = functionContext.evaluate( closure.getBody(), functionEnvironment);

      functionContext.exit();

      return result;
    } catch(ReturnException e) {
      if(e.getEnvironment() != functionEnvironment) {
        throw e;
      }
      return e.getValue();
    }
  }

  /* Create a promise to evaluate each argument.  Although this is most */
/* naturally attacked with a recursive algorithm, we use the iterative */
/* form below because it is does not cause growth of the pointer */
/* protection stack, and because it is a little more efficient. */

  public static PairList promiseArgs(PairList el, Context context, Environment rho)
  {
    PairList.Builder list = new PairList.Builder();

    for(PairList.Node node : el.nodes()) {

      /* If we have a ... symbol, we look to see what it is bound to.
      * If its binding is Null (i.e. zero length)
      * we just ignore it and return the cdr with all its
      * expressions promised; if it is bound to a ... list
      * of promises, we repromise all the promises and then splice
      * the list of resulting values into the return value.
      * Anything else bound to a ... symbol is an error
      */

      /* Is this double promise mechanism really needed? */

      if (node.getValue().equals(Symbols.ELLIPSES)) {
        PromisePairList dotExp = (PromisePairList)rho.findVariable(Symbols.ELLIPSES);
        for(PairList.Node dotNode : dotExp.nodes()) {
          list.add(dotNode.getRawTag(), dotNode.getValue());
        }
      } else if (node.getValue() == Symbol.MISSING_ARG) {
        list.add(node.getRawTag(), node.getValue());
      } else {
        if(node.getValue() instanceof Promise) {
          list.add(node.getRawTag(), node.getValue());
        } else {
          list.add(node.getRawTag(), Promise.repromise(rho, node.getValue()));
        }
      }
    }
    return list.build();
  }

  /*  usemethod  -  calling functions need to evaluate the object
 *  (== 2nd argument).  They also need to ensure that the
 *  argument list is set up in the correct manner.
 *
 *    1. find the context for the calling function (i.e. the generic)
 *       this gives us the unevaluated arguments for the original call
 *
 *    2. create an environment for evaluating the method and insert
 *       a handful of variables (.Generic, .Class and .Method) into
 *       that environment. Also copy any variables in the env of the
 *       generic that are not formal (or actual) arguments.
 *
 *    3. fix up the argument list; it should be the arguments to the
 *       generic matched to the formals of the method to be invoked */

  public static PairList stripDefaultValues(PairList formals) {
    PairList.Builder result = new PairList.Builder();
    for(PairList.Node node : formals.nodes()) {
      result.add(node.getRawTag(), Symbol.MISSING_ARG);
    }
    return result.build();
  }


  public static void matchArgumentsInto(PairList formals, PairList actuals, Context innerContext, Environment innerEnv) {

    PairList matched = matchArguments(formals, actuals, true);
    for(PairList.Node node : matched.nodes()) {
      SEXP value = node.getValue();
      if(value == Symbol.MISSING_ARG) {
        SEXP defaultValue = formals.findByTag(node.getTag());
        if(defaultValue != Symbol.MISSING_ARG) {
          value =  Promise.repromise(innerEnv, defaultValue);
        }
      }
      innerEnv.setVariable(node.getTag(), value);
    }
  }

  /**
   * Argument matching is done by a three-pass process:
   * <ol>
   * <li><strong>Exact matching on tags.</strong> For each named supplied argument the list of formal arguments
   *  is searched for an item whose name matches exactly. It is an error to have the same formal
   * argument match several actuals or vice versa.</li>
   *
   * <li><strong>Partial matching on tags.</strong> Each remaining named supplied argument is compared to the
   * remaining formal arguments using partial matching. If the name of the supplied argument
   * matches exactly with the first part of a formal argument then the two arguments are considered
   * to be matched. It is an error to have multiple partial matches.
   *  Notice that if f <- function(fumble, fooey) fbody, then f(f = 1, fo = 2) is illegal,
   * even though the 2nd actual argument only matches fooey. f(f = 1, fooey = 2) is legal
   * though since the second argument matches exactly and is removed from consideration for
   * partial matching. If the formal arguments contain ‘...’ then partial matching is only applied to
   * arguments that precede it.
   *
   * <li><strong>Positional matching.</strong> Any unmatched formal arguments are bound to unnamed supplied arguments,
   * in order. If there is a ‘...’ argument, it will take up the remaining arguments, tagged or not.
   * If any arguments remain unmatched an error is declared.
   *
   * @param actuals the actual arguments supplied to the list
   * @param populateMissing
   */
  public static PairList matchArguments(PairList formals, PairList actuals, boolean populateMissing) {

    PairList.Builder result = new PairList.Builder();

    List<PairList.Node> unmatchedActuals = Lists.newArrayList();
    for(PairList.Node argNode : actuals.nodes()) {
      unmatchedActuals.add(argNode);
    }

    List<PairList.Node> unmatchedFormals = Lists.newArrayList(formals.nodes());

    boolean hasEllipses = false;
    
    // do exact matching
    for(ListIterator<PairList.Node> formalIt = unmatchedFormals.listIterator(); formalIt.hasNext(); ) {
      PairList.Node formal = formalIt.next();
      if(formal.hasTag()) {
        Symbol name = formal.getTag();
        if(name == Symbols.ELLIPSES) {
          hasEllipses = true;
        }
        Collection<PairList.Node> matches = Collections2.filter(unmatchedActuals, PairList.Predicates.matches(name));

        if(matches.size() == 1) {
          PairList.Node match = first(matches);
          result.add(name, match.getValue());
          formalIt.remove();
          unmatchedActuals.remove(match);

        } else if(matches.size() > 1) {
          throw new EvalException(String.format("Multiple named values provided for argument '%s'", name.getPrintName()));
        }
      }
    }

    // do partial matching, as long as there is no ellipses (...) argument
    if(!hasEllipses) {
      Collection<PairList.Node> remainingNamedFormals = filter(unmatchedFormals, PairList.Predicates.hasTag());
      for (Iterator<PairList.Node> actualIt = unmatchedActuals.iterator(); actualIt.hasNext(); ) {
        PairList.Node actual = actualIt.next();
        if (actual.hasTag()) {
          Collection<PairList.Node> matches = Collections2.filter(remainingNamedFormals,
                  PairList.Predicates.startsWith(actual.getTag()));

          if (matches.size() == 1) {
            PairList.Node match = first(matches);
            result.add(match.getTag(), actual.getValue());
            actualIt.remove();
            unmatchedFormals.remove(match);

          } else if (matches.size() > 1) {
            throw new EvalException(String.format("Provided argument '%s' matches multiple named formal arguments: %s",
                    actual.getTag().getPrintName(), argumentTagList(matches)));
          }
        }
      }
    }

    // match any unnamed args positionally

    Iterator<PairList.Node> formalIt = unmatchedFormals.iterator();
    PeekingIterator<PairList.Node> actualIt = Iterators.peekingIterator(unmatchedActuals.iterator());
    while( formalIt.hasNext()) {
      PairList.Node formal = formalIt.next();
      if(Symbols.ELLIPSES.equals(formal.getTag())) {
        PromisePairList.Builder promises = new PromisePairList.Builder();
        while(actualIt.hasNext()) {
          PairList.Node actual = actualIt.next();
          promises.add( actual.getRawTag(),  actual.getValue() );
        }
        result.add(formal.getTag(), promises.build() );

      } else if( hasNextUnTagged(actualIt) ) {
        result.add(formal.getTag(), nextUnTagged(actualIt).getValue() );

      } else if(populateMissing) {
        result.add(formal.getTag(), Symbol.MISSING_ARG);
      }
    }
    if(actualIt.hasNext()) {
      throw new EvalException(String.format("Unmatched positional arguments"));
    }

    return result.build();
  }


  private static boolean hasNextUnTagged(PeekingIterator<PairList.Node> it) {
    return it.hasNext() && !it.peek().hasTag();
  }

  private static PairList.Node nextUnTagged(Iterator<PairList.Node> it) {
    PairList.Node arg = it.next() ;
    while( arg.hasTag() ) {
      arg = it.next();
    }
    return arg;
  }

  private static String argumentTagList(Collection<PairList.Node> matches) {
    return Joiner.on(", ").join(transform(matches, new CollectionUtils.TagName()));
  }

  private static <X> X first(Iterable<X> values) {
    return values.iterator().next();
  }

}
