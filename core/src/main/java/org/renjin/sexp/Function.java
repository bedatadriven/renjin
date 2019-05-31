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

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;

/**
 * Superinterface for the three function-like {@code SEXP}s:
 * {@code Closure}, {@code SpecialFunction}, and {@code PrimitiveFunction}.
 *
 * 
 */
public interface Function extends SEXP, Recursive {

  public static final String IMPLICIT_CLASS = "function";

  SEXP apply(Context originalContext, Environment environment, FunctionCall call);

  /**
   *
   * Applies the function with the given arguments.
   *
   * <p>The arguments as passed to the function must be <i>expanded</i>. That is, the symbol '...' should not
   * be passed, but rather resolved and the array of argument names and values expanded to include in the array.</p>
   *
   * <p>Missing positional arguments, such as the first argument in the call g(, 1), should be passed as
   * {@code Symbol.MISSING_ARG}, not a promise.</p>
   *
   * <p>The arguments, as passed to this method, are not yet matched: they should be in the original order.</p>
   *
   * <p>The promisedArguments array may be modified by the callee.</p>
   *
   * <p>The argumentNames array must not be modified.</p>
   *
   * @param context the context from which this function is being called
   * @param rho the function call's environment
   * @param call the original function call
   * @param argumentNames the names of the arguments
   * @param promisedArguments an array of the arguments, promised in the caller's environment
   * @param dispatch
   * @return the function's result
   */
  SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);


}
