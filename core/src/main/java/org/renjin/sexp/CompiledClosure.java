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
package org.renjin.sexp;

import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CompiledClosure extends Closure {
  private final String compiledClassName;
  private final String compiledMethodName;
  private MethodHandle methodHandle;
  private ArgumentMatcher argumentMatcher;

  public CompiledClosure(Environment enclosingEnvironment, PairList formals, SEXP body,
                         AttributeMap attributes,
                         String compiledClassName,
                         String compiledMethodName) {
    super(enclosingEnvironment, formals, body, attributes);
    this.compiledClassName = compiledClassName;
    this.compiledMethodName = compiledMethodName;
  }


  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    if(argumentMatcher == null) {
      argumentMatcher = new ArgumentMatcher(getFormals());
    }

    if(methodHandle == null) {
      try {
        methodHandle = MethodHandles.publicLookup().findStatic(Class.forName(compiledClassName), compiledMethodName,
            MethodType.methodType(SEXP.class, Context.class, Environment.class, SEXP[].class));
      } catch (Throwable e) {
        throw new RuntimeException("Could not obtain method handle for " + compiledClassName + "." + compiledMethodName, e);
      }
    }

    MatchedArguments matched = argumentMatcher.match(args);
    SEXP[] arguments = new SEXP[matched.getFormalCount()];
    for (int i = 0; i < matched.getFormalCount(); i++) {
      int actualIndex = matched.getActualIndex(i);
      if(actualIndex >= 0) {
        SEXP actualValue = matched.getActualValue(actualIndex);
        Promise promisedValue = Promise.repromise(rho, actualValue);
        arguments[i] = promisedValue;
      }
    }

    try {
      return (SEXP)methodHandle.invokeExact(context, getEnclosingEnvironment(), arguments);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException(e);
    }
  }
}
