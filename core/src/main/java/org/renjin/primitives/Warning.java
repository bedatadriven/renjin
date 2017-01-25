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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.Options;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.PrintWriter;


public class Warning {

  public static final Symbol LAST_WARNING = Symbol.get("last.warning");

  public static void invokeWarning(@Current Context context, String message, Object... args) {
    emitWarning(context, null, false, String.format(message, args));
  }

  public static void invokeWarning(@Current Context context, FunctionCall call, String message, Object... args) {
    emitWarning(context, call, false, String.format(message, args));
  }

  @Internal
  public static void warning(@Current Context context, boolean call, boolean immediate, String message) throws IOException {

    if(call || !immediate) {
      FunctionCall currentCall = findCurrentCall(context, 1);

      emitWarning(context, currentCall, immediate, message);

    } else {
      PrintWriter stderr = context.getSession().getEffectiveStdErr();
      stderr.println("Warning message:");
      stderr.println(message);
    }
  }

  /**
   * Finds the FunctionCall from which the warning was emitted, or null if 
   * warning was invoked from a top level context.
   */
  private static FunctionCall findCurrentCall(Context context, int toSkip) {
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
    return null;
  }

  public static void emitWarning(Context context, boolean immediate, String message) {
    emitWarning(context, findCurrentCall(context, 0), immediate, message);
  }

  public static void emitWarning(Context context, FunctionCall call,  boolean immediate, String message)  {

    // Create the condition object
    ListVector.NamedBuilder condition = new ListVector.NamedBuilder();
    condition.setAttribute(Symbols.CLASS, new StringArrayVector("simpleWarning", "warning", "condition"));
    condition.add("message", message);
    if(call != null) {
      condition.add("call", call);
    }

    // Try signaling the condition
    Conditions.signalCondition(context, condition.build(), message, call);

    // If ConditionException is not thrown, then proceed with default behavior
    uncaughtWarning(context, call, immediate, message);
  }

  /**
   * Handle a warning that is not caught by any condition handlers installed by the user
   */
  private static void uncaughtWarning(Context context, FunctionCall call, boolean immediate, String message) {
    int warnMode = context.getSession().getSingleton(Options.class).getInt("warn", 0);
    if(warnMode == 1 || (warnMode <= 0 && immediate)) {
      PrintWriter stderr = context.getSession().getEffectiveStdErr();
      stderr.println("Warning in " + call.toString() + " :");
      stderr.println("  " + message);
    } else if(warnMode == 0) {
      // store warnings until end of evaluation

      ListVector.NamedBuilder lastWarning = new ListVector.NamedBuilder();

      Environment baseEnv = context.getBaseEnvironment();
      if(baseEnv.hasVariable(LAST_WARNING)) {
        lastWarning.addAll((ListVector)baseEnv.getVariable(LAST_WARNING).force(context));
      }
      if(call != null) {
        lastWarning.add(message, call);
      } else {
        lastWarning.add(message, Null.INSTANCE);
      }
      baseEnv.setVariable(LAST_WARNING, lastWarning.build());
    }
  }

  @Internal
  public static void printDeferredWarnings(@Current Context context) {
    context.getSession().getEffectiveStdErr().println("In addition: (TODO)");
  }

}
