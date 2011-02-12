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

package r.lang.primitive.special;

import r.lang.*;
import r.lang.exception.EvalException;

public class OnExitFunction extends SpecialFunction {

  @Override
  public String getName() {
    return "on.exit";
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {
    EvalException.check(call.getArguments().length() == 1 || call.getArguments().length() == 2,
        "invalid number of arguments");

    SEXP value = call.getArgument(0);
    boolean add = false;
    if(call.getArguments().length() == 2) {
      add = call.evalArgument(context, rho, 1).asReal() != 0;
    }

    if(add) {
      context.addOnExit(value);
    } else {
      context.setOnExit(value);
    }
    return EvalResult.NON_PRINTING_NULL;
  }
}
