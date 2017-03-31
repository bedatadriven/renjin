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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.packaging.SerializedPromise;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.Map;

/**
 * Provides access to the runtime environment at the moment of compilation,
 * recording all lookups as assumptions that can be tested at future moments
 * in the runtime.
 */
public class RuntimeState {
  private Context context;
  private Environment rho;


  /**
   * List of symbols that we have resolved to builtins / or inlined
   * closures. We need to check at the end that there is no possiblity
   * they have been assigned to.
   */
  private Map<Symbol, Function> resolvedFunctions = Maps.newHashMap();

  public RuntimeState(Context context, Environment rho) {
    this.context = context;
    this.rho = rho;
  }

  public RuntimeState(RuntimeState parentState, Environment enclosingEnvironment) {
    this(parentState.context, enclosingEnvironment);
  }

  public PairList getEllipsesVariable() {
    SEXP ellipses = rho.getVariable(context, Symbols.ELLIPSES);
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new InvalidSyntaxException("'...' used in an incorrect context.");
    }
    return (PairList) ellipses;
  }

  public SEXP findVariable(Symbol name) {

    SEXP value = null;
    Environment environment = rho;
    while(environment != Environment.EMPTY) {
      if (environment.isActiveBinding(name)) {
        throw new NotCompilableException(name, "Active Binding encountered");
      }
      value = rho.findVariable(context, name);
      if(value instanceof Promise) {
        Promise promisedValue = (Promise) value;
        if(promisedValue.isEvaluated()) {
          value = promisedValue.force(context);
        } else {
          // Promises can have side effects, and evaluation order is important
          // so we can't just force all the promises in the beginning of the loop
          throw new NotCompilableException(name, "Unevaluated promise encountered");
        }
      }
      environment = environment.getParent();
    }
    if(value == null) {
      throw new NotCompilableException(name, "Symbol not found. Should not reach here!");
    }
    return value;
  }


  public Function findFunction(Symbol functionName) {

    Function f = findFunctionIfExists(functionName);
    if (f != null) {
      return f;
    }
    throw new NotCompilableException(functionName, "Could not find function " + functionName);
  }

  public Function findFunctionIfExists(Symbol functionName) {
    if(resolvedFunctions.containsKey(functionName)) {
      return resolvedFunctions.get(functionName);
    }

    Environment environment = rho;
    while(environment != Environment.EMPTY) {
      Function f = isFunction(functionName, environment.getVariable(context, functionName));
      if(f != null) {
        resolvedFunctions.put(functionName, f);
        return f;
      }
      environment = environment.getParent();
    }
    return null;
  }

  /**
   * Tries to safely determine whether the expression is a function, without
   * forcing any promises that might have side effects.
   * @param exp
   * @return null if the expr is definitely not a function, or {@code expr} if the
   * value can be resolved to a Function without side effects
   * @throws NotCompilableException if it is not possible to determine
   * whether the value is a function without risking side effects.
   */
  private Function isFunction(Symbol functionName, SEXP exp) {
    if(exp instanceof Function) {
      return (Function)exp;

    } else if(exp instanceof SerializedPromise) {
      // Functions loaded from packages are initialized stored as SerializedPromises
      // so the AST does not have to be loaded into memory until the function is first 
      // needed. Though technically this does involve I/O and thus side-effects,
      // we don't consider it to have any affect on the running program, and so
      // can safely force it.
      return isFunction(functionName, exp.force(this.context));

    } else if(exp instanceof Promise) {
      Promise promise = (Promise)exp;
      if(promise.isEvaluated()) {
        return isFunction(functionName, promise.getValue());
      } else {
        throw new NotCompilableException(functionName, "Symbol " + functionName + " cannot be resolved to a function " +
            " an enclosing environment has a binding of the same name to an unevaluated promise");
      }
    } else {
      return null;
    }
  }

  public Map<Symbol, Function> getResolvedFunctions() {
    return resolvedFunctions;
  }
}
