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
import r.lang.exception.FunctionCallException;

public class OpsFunction extends BuiltinFunction {

  public OpsFunction(String name) {
    super(name, Ops.class);
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {

    PairList evaluated = Calls.evaluateList(context, rho, args);

    EvalResult dispatched = Calls.DispatchGroup("Ops",call, this, evaluated, context, rho);
    if(dispatched != null) {
      return dispatched;
    }

    // otherwise execute with builtin functions
    try {
      return RuntimeInvoker.INSTANCE.invoke(context, rho, call, evaluated, getOverloads());
    } catch (EvalException e) {
      throw new FunctionCallException(call, args, e);
    }
  }
}
