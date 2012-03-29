/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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
