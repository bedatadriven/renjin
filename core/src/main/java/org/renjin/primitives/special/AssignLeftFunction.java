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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


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
      SEXP getter = call.getFunction();
      SEXP setter = setterFromGetter(getter);

      PairList setterArgs = PairList.Node.newBuilder()
          .addAll(call.getArguments())
          .add("value", rhs)
          .build();
      
      FunctionCall setterCall = new FunctionCall(setter, setterArgs);
      
      rhs = Promise.repromise(context.evaluate(setterCall, rho));

      lhs = call.getArgument(0);
    }

    Symbol target;
    if( lhs instanceof Symbol) {
      target = (Symbol) lhs;
    } else if(lhs instanceof StringVector) {
      target = Symbol.get(((StringVector) lhs).getElementAsString(0));
    } else {
      throw new EvalException("cannot assign to value '" + lhs + " (of type " + lhs.getTypeName() + ")");
    }

    // make the final assignment to the target symbol
    if(rhs instanceof Promise) {
      rhs = rhs.force(context);
    }
    assignResult(context, rho, target, rhs);

    context.setInvisibleFlag();
    
    return evaluatedValue;
  }

  private SEXP setterFromGetter(SEXP getter) {
    if(getter instanceof Symbol) {
      return Symbol.get(((Symbol) getter).getPrintName() + "<-");
    }

    if(getter instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) getter;
      if(call.getArguments().length() == 2 &&
          (call.getFunction() == Symbol.get("::") ||
           call.getFunction() == Symbol.get(":::"))) {
        SEXP namespace = call.getArgument(0);
        SEXP namespacedGetter = call.getArgument(1);
        SEXP setter = setterFromGetter(namespacedGetter);
        return FunctionCall.newCall(call.getFunction(), namespace, setter);
      }
    }
    throw new EvalException("invalid function in complex assignment");
  }

  protected void assignResult(Context context, Environment rho, Symbol target, SEXP rhs) {
    if(target.isReservedWord() && rhs instanceof Function) {
      context.warn("Renjin does not honor redefinition of '" + target.getPrintName() + "' function");
    }
    rho.setVariable(target, rhs);
  }
}
