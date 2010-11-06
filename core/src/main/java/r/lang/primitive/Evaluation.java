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

package r.lang.primitive;

import com.google.common.annotations.VisibleForTesting;
import r.lang.*;
import r.lang.exception.ControlFlowException;
import r.lang.exception.EvalException;

public class Evaluation {


  /**
   * Evaluates a list of statements. (The '{' function)
   */
  public static EvalResult begin(EnvExp rho, LangExp call) {

    EvalResult lastResult = new EvalResult(NilExp.INSTANCE, true);
    for (SEXP sexp : call.getArguments()) {
      lastResult = sexp.evaluate(rho);
    }
    return lastResult;
  }

  public static EvalResult assign(EnvExp rho, LangExp call) {
    return new EvalResult( assign(rho, call.getArgument(0), call.getArgument(1)), false);
  }

  /**
   * Executes an assignment. (The '<-' function)
   */
  @VisibleForTesting
  static SEXP assign(EnvExp rho, SEXP leftHand, SEXP rightHand) {

    SymbolExp target = toSymbol(rho, leftHand);
    SEXP newValue = rightHand.evalToExp(rho);

    rho.setVariable(target, newValue);

    return newValue;
  }

  private static SymbolExp toSymbol(EnvExp rho, SEXP lhs) {
    if(lhs instanceof SymbolExp) {
      return (SymbolExp) lhs;

    } else if(lhs instanceof StringExp) {
      String name = ((StringExp) lhs).get(0);
      return rho.getGlobalContext().getSymbolTable().install(name);

    } else {
      throw new EvalException("invalid (do_set) left-hand side to assignment");
    }
  }

  /**
   * for ( x in elements ) { statement }
   */
  public static void forLoop(EnvExp rho, LangExp call) {
    PairList args = call.getArguments();
    SymbolExp symbol = (SymbolExp) args.getFirst();
    SEXP elements = args.getSecond().evalToExp(rho);
    SEXP statement = args.getThird();

    for(int i=0; i!=elements.length(); ++i) {
      rho.setVariable(symbol, elements.subset(1));
      statement.evaluate(rho);
    }
  }

  public static void whileLoop(EnvExp rho, LangExp call) {
    PairList args = call.getArguments();
    SEXP condition = args.getFirst();
    SEXP statement = args.getSecond();

    while(asLogicalNoNA(call, condition.evaluate(rho).getExpression(), rho)) {

      try {

        statement.evaluate(rho);

      } catch(BreakException e) {
        break;
      }
    }
  }


  /**
   * function()
   */
  public static ClosureExp function( EnvExp rho, LangExp call ) {
    PairList args = call.getArguments();
    return new ClosureExp(rho, (PairList) args.getFirst(), args.getSecond());
  }

  /**
   * if()
   */
  public static EvalResult doIf(EnvExp rho, LangExp call) {
    SEXP condition = call.getArguments().get(0).evalToExp(rho);

    if (asLogicalNoNA(call, condition, rho)) {
      return new EvalResult(call.getArguments().get(1).evalToExp(rho)); /* true value */

    } else {
      if (call.getArguments().length() == 3) {
        return new EvalResult(call.getArguments().get(2).evalToExp(rho)); /* else value */
      } else {
        return EvalResult.NON_PRINTING_NULL;   /* no else, evaluates to NULL */
      }
    }
  }


  public static EvalResult internal(EnvExp rho, LangExp call) {
    SEXP arg = call.getArguments().get(0);
    if(!(arg instanceof LangExp)) {
      throw new EvalException("invalid .Internal() argument");
    }
    LangExp internalCall = (LangExp) arg;
    SymbolExp fnSymbol = (SymbolExp)internalCall.getFunction();
    if(fnSymbol.getInternal() == NilExp.INSTANCE) {
      throw new EvalException(String.format("no internal function \"%s\"", fnSymbol.getPrintName()));
    }
    FunExp fn = (FunExp) fnSymbol.getInternal();
    return fn.apply(internalCall, internalCall.getArguments(), rho);
  }


  public EvalResult next() {
    throw new NextException();
  }
  /**
   * break;
   */
  public static void doBreak() {
    throw new BreakException();
  }

  public static EvalResult doReturn(SEXP value) {
    throw new ReturnException(value);
  }

  public static boolean asLogicalNoNA(LangExp call, SEXP s, EnvExp rho) {

    if (s.length() > 1) {
      rho.getGlobalContext().warningCall(call, "the condition has length > 1 and only the first element will be used");
    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;

  }

  public static SEXP missing(EnvExp rho, LangExp call) {
    PairList args = call.getArguments();
    SymbolExp symbol;
    try {
      symbol = (SymbolExp) args.getFirst();
    } catch (ClassCastException e) {
      throw new EvalException(call, "invalid use of 'missing'");
    }
    SEXP value = rho.findVariable(symbol);
    if(value == SymbolExp.UNBOUND_VALUE) {
      throw new EvalException(call, "'missing' can only be used for arguments");

    } else if(value == SymbolExp.MISSING_ARG) {
      return new LogicalExp(true);

    } else {
      return new LogicalExp(false);
    }
  }

  public static class BreakException extends ControlFlowException {


  }

  public static class NextException extends ControlFlowException {
  }

  public static class ReturnException extends ControlFlowException {

    private final SEXP value;

    public ReturnException(SEXP value) {
      this.value = value;
    }

    public SEXP getValue() {
      return value;
    }
  }
}
