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

package r.base;

import r.jvmi.binding.RuntimeInvoker;
import r.lang.*;
import r.lang.exception.EvalException;

/**
 * Function class for the Ops group of builtin functions.
 *
 * Ops functions are group generic, which means that the evaluation environment ({@code rho})
 * is checked for +.{class name 1}, Ops.{class name 1}, +.{class name 2}, Ops.{class name 2}
 * before the default implementation is invoked.
 *
 */
public class OpsFunction extends BuiltinFunction {

  public OpsFunction(String name) {
    super(name, Ops.class);
  }

  @Override
  public final EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {

    PairList evaluated = Calls.evaluateList(context, rho, args);

    EvalResult dispatched = Calls.DispatchGroup("Ops",call, this, evaluated, context, rho);
    if(dispatched != null) {
      return dispatched;
    }

    // otherwise execute with builtin functions
    try {
      return applyDefault(context, rho, call, evaluated);
    } catch (EvalException e) {
      if(e.getContext() == null) {
        e.initContext(context);
      }
      throw e;
    }
  }

  /**
   * Applies the default builtin function using reflection after no matching overrides are found.
   * Generated subclasses can override this method to provide a more efficient implementation.
   */
  protected EvalResult applyDefault(Context context, Environment rho, FunctionCall call, PairList evaluated) {
    return RuntimeInvoker.INSTANCE.invoke(context, rho, call, evaluated, getOverloads());
  }
}
