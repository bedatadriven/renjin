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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Options;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.io.PrintWriter;


public class Warning {

  public static final Symbol LAST_WARNING = Symbol.get("last.warning");

  /**
   * Implementation of the .Internal(warning) R function.
   *
   * <p>Finds the call in which the warning was raised and then delegates to
   * {@link #defaultWarning(Context, String, SEXP, boolean)}</p>
   *
   * @param context the current evaluation Context.
   * @param call {@code true} if the call should become part of the warning message
   * @param immediate {@code true} if the warning should be output, even if {@code getOption("warn") <= 0}
   * @param message the warning message
   */
  @Internal
  public static void warning(@Current Context context, boolean call, boolean immediate, boolean noBreaks, String message) {

    SEXP callSexp;
    if(call) {
      callSexp = findCurrentCall(context, 1);
    } else {
      callSexp = Null.INSTANCE;
    }
    warning(context, callSexp, immediate, message);
  }

  /**
   * Initiates a warning in the given R context.
   *
   * <p>This method delegates to the R function {@code .signalSimpleWarning} which constructs and signals
   * a simple warning condition.
   *
   * <p>If no restarts are invoked, {@code .signalSimpleWarning} proceeds to invoke
   * {@link #defaultWarning(Context, String, SEXP, boolean)} via {@code .Internal(.dftlWarning())}</p>
   *
   *
   * @param context the evaluation context
   * @param call the {@link FunctionCall} in which the warning was raised, or {@code Null.INSTANCE}
   * @param immediate true if the warning should be printed to the console immediately, or {@code false} if it
   *                  should be collected until the end of the current evaluation loop. Overrides {@code option(warn)}
   * @param message the warning message
   */
  public static void warning(Context context, SEXP call, boolean immediate, String message) {

    SEXP quoteCall;
    if(call == Null.INSTANCE) {
      quoteCall = Null.INSTANCE;
    } else {
      quoteCall = FunctionCall.newCall(Symbol.get("quote"), call);
    }

    FunctionCall signalCall = FunctionCall.newCall(Symbol.get(".signalSimpleWarning"),
        StringVector.valueOf(message),
        quoteCall,
        LogicalVector.valueOf(immediate));

    context.evaluate(signalCall, context.getGlobalEnvironment());
  }

  /**
   * Finds the FunctionCall from which the warning was emitted, or null if 
   * warning was invoked from a top level context.
   */
  private static SEXP findCurrentCall(Context context, int toSkip) {
    while(!context.isTopLevel()) {
      if(context.getCall() != null) {
        if(toSkip == 0) {
          return context.getCall();
        }
        toSkip --;
      }
      context = context.getParent();
    }
    // we were at the top level
    return Null.INSTANCE;
  }

  /**
   * Implements the "default" behavior of a warning.
   *
   * <p>This method is invoked by the R function {@code .signalSimpleWarning}, or by the R base function
   * {@code warning} if a condition object is provided.
   */
  @Internal(".dfltWarn")
  public static void defaultWarning(@Current Context context, String message, SEXP call, boolean immediate) {

    // Step 0: TODO
    // Ignore warnings raised during the handling of other warnings.


    // Step 1: check for presence of option(warning.expression)
    // If present, delegate and exit.

    SEXP warningExpression = context.getSession().getSingleton(Options.class).get("warning.expression");
    if(warningExpression != Null.INSTANCE) {
      context.evaluate(warningExpression, context.getGlobalEnvironment());
      return;
    }

    // Step 2: Consult the global warning level

    int warnMode = context.getSession().getSingleton(Options.class).getInt("warn", 0);
    if(IntVector.isNA(warnMode)) {
      warnMode = 0;
    }

    // Step 2a: If options('warn') >= 2, convert the warning to an error
    if(warnMode >= 2) {
      throw new EvalException(String.format("(converted from warning) %s", message));
    }

    // Step 2b: If options('warn') == 1, or immediate flag is set, print immediately

    if(warnMode == 1 || (warnMode <= 0 && immediate)) {
      PrintWriter stderr = context.getSession().getEffectiveStdErr();
      stderr.println("Warning in " + call.toString() + " :");
      stderr.println("  " + message);
      return;

    }

    // Step 2c: Otherwise, collect the warning for printing at the end of the current statement

    ListVector.NamedBuilder lastWarning = new ListVector.NamedBuilder();

    Environment baseEnv = context.getBaseEnvironment();
    if(baseEnv.hasVariable(LAST_WARNING)) {
      lastWarning.addAll((ListVector)baseEnv.getVariable(context, LAST_WARNING).force(context));
    }
    lastWarning.add(message, call);
    baseEnv.setVariable(context, LAST_WARNING, lastWarning.build());
  }

  @Internal
  public static void printDeferredWarnings(@Current Context context) {
    context.getSession().getEffectiveStdErr().println("In addition: (TODO)");
  }

}
