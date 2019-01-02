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

/**
 * Superinterface for the three function-like {@code SEXP}s:
 * {@code Closure}, {@code SpecialFunction}, and {@code PrimitiveFunction}.
 *
 * 
 */
public interface Function extends SEXP, Recursive {

  public static final String IMPLICIT_CLASS = "function";

  /**
   * 
   * @param context the context from which this function is being called
   * @param rho the environment from which this function is being called
   * @param call the original function call
   * @param args UNEVALUATED arguments
   * @return the function's result
   */
  SEXP apply(Context context, Environment rho, FunctionCall call, PairList args);
}
