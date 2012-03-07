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

package org.renjin.primitives.special;

import r.compiler.ir.exception.InvalidSyntaxException;
import r.lang.*;
import r.lang.exception.EvalException;

public class AssignLeftFunction extends SpecialFunction {

  public AssignLeftFunction() {
    super("<-");
  }
  
  protected AssignLeftFunction(String name) {
    super(name);
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    SEXP lhs = call.getArgument(0);
    SEXP rhs = call.getArgument(1);

    return assignLeft(context, rho, lhs, rhs);
  }


  /** It's important that the rhs get evaluated first because
   * assignment is right associative i.e.  a <- b <- c is parsed as
   * a <- (b <- c).
   */
  public SEXP assignLeft(Context context, Environment rho,
                                      SEXP lhs, SEXP value) {

    // this loop handles nested, complex assignments, such as:
    // class(x) <- "foo"
    // x$a[3] <- 4
    // class(x$a[3]) <- "foo"

    SEXP evaluatedValue = context.evaluate( value, rho);
    SEXP rhs = new Promise(value, evaluatedValue);

    while(lhs instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) lhs;
      Symbol getter = (Symbol) call.getFunction();
      Symbol setter = Symbol.get(getter.getPrintName() + "<-");

      rhs = context.evaluate( new FunctionCall(setter,
          PairList.Node.newBuilder()
            .addAll(call.getArguments())
            .add("value", rhs)
            .build()), rho);

      lhs = call.getArgument(0);
    }

    Symbol target;
    if( lhs instanceof Symbol) {
      target = (Symbol) lhs;
    } else if(lhs instanceof StringVector) {
      target = Symbol.get(((StringVector) lhs).getElement(0));
    } else {
      throw new InvalidSyntaxException("cannot assign to value '" + lhs + " (of type " + lhs.getTypeName() + ")");
    }

    // make the final assignment to the target symbol
    if(rhs instanceof Promise) {
      rhs = ((Promise) rhs).force();
    }
    assignResult(rho, target, rhs);

    context.setInvisibleFlag();
    
    return evaluatedValue;
  }

  protected void assignResult(Environment rho, Symbol target, SEXP rhs) {
    rho.setVariable(target, rhs);
  }
}
