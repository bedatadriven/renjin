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

import r.lang.*;
import r.lang.exception.ControlFlowException;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Environment;
import r.lang.primitive.annotations.Evaluate;

public class Evaluation {


  /**
   * Evaluates a list of statements. (The '{' function)
   */
  public static EvalResult begin(EnvExp rho, LangExp call) {

    EvalResult lastResult = new EvalResult(NullExp.INSTANCE, true);
    for (SEXP sexp : call.getArguments()) {
      lastResult = sexp.evaluate(rho);
    }
    return lastResult;
  }

  public static SEXP paren(SEXP value) {
    return value;
  }


  public static EvalResult assignLeft(@Environment EnvExp rho, SymbolExp symbol, SEXP value) {
    rho.setVariable(symbol, value);
    return new EvalResult(value, false);
  }

  /**
   * There are no restrictions on name: it can be a non-syntactic name (see make.names).
   *
   * The pos argument can specify the environment in which to assign the object in any
   * of several ways: as an integer (the position in the search list); as the character
   * string name of an element in the search list; or as an environment (including using
   * sys.frame to access the currently active function calls). The envir argument is an
   *  alternative way to specify an environment, but is primarily there for back compatibility.
   *
   * assign does not dispatch assignment methods, so it cannot be used to
   *  set elements of vectors, names, attributes, etc.
   *
   * Note that assignment to an attached list or data frame changes the attached copy
   *  and not the original object: see attach and with.
   */
  public static EvalResult assign(String name, SEXP value, EnvExp environ, boolean inherits) {

    SymbolExp symbol = new SymbolExp(name);
    if(!inherits) {
      environ.setVariable(symbol, value);
    } else {
      while(environ != EnvExp.EMPTY && !environ.hasVariable(symbol)) {
        environ = environ.getParent();
      }
      if(environ == EnvExp.EMPTY) {
        environ.getGlobalEnvironment().setVariable(symbol, value);
      } else {
        environ.setVariable(symbol, value);
      }
    }
    return EvalResult.nonVisible(value);
  }


  /**
   * This is the so-called complex assignment, such as:
   *  class(x) <- "foo" or
   *  length(x) <- 3
   *
   *
   */
  public static EvalResult assignLeft(@Environment EnvExp rho, @Evaluate(false) LangExp call, SEXP value) {
    SymbolExp functionName = (SymbolExp) call.getFunction();
    SymbolExp newFn = new SymbolExp(functionName.getPrintName() + "<-");
                          
    // This is the symbol to which we're ultimately assigning
    SymbolExp target = call.getArgument(0);

    SEXP result = LangExp.newCall(newFn, target, value)
                         .evaluate(rho)
                         .getExpression();

    rho.setVariable(target, result);

    return EvalResult.nonVisible(result);
  }

  public static void onExit( @Environment EnvExp rho, @Evaluate(false) SEXP exp, boolean add ) {
    if(add) {
      rho.addOnExit(exp);
    } else {
      rho.setOnExit(exp);
    }
  }

  public static void onExit( @Environment EnvExp rho, @Evaluate(false) SEXP exp) {
    rho.setOnExit(exp);
  }

  /**
   * for ( x in elements ) { statement }
   */
  public static void forLoop(EnvExp rho, LangExp call) {
    PairList args = call.getArguments();
    SymbolExp symbol = (SymbolExp) args.getFirst();
    SEXP elements = args.getSecond().evalToExp(rho);
    SEXP statement = args.getThird();

    for(int i=1; i<=elements.length(); ++i) {
      rho.setVariable(symbol, elements.subset(i));
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
      return call.getArguments().get(1).evaluate(rho); /* true value */

    } else {
      if (call.getArguments().length() == 3) {
        return call.getArguments().get(2).evaluate(rho); /* else value */
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
    SymbolExp internalName = (SymbolExp)internalCall.getFunction();
    SEXP function = rho.findInternal(internalName);

    if(function == NullExp.INSTANCE) {
      throw new EvalException(String.format("no internal function \"%s\"", internalName.getPrintName()));
    }
    return ((FunExp)function).apply(internalCall, internalCall.getArguments(), rho);
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

  public static EvalResult eval(SEXP expression, EnvExp environment,
                                SEXP enclosing /* ignored */) {
    return expression.evaluate(environment);
  }

  public static SEXP quote(@Evaluate(false) SEXP exp) {
    return exp;
  }

  public static boolean asLogicalNoNA(LangExp call, SEXP s, EnvExp rho) {

    if (s.length() > 1) {
      Warning.warning(call, "the condition has length > 1 and only the first element will be used");
    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;
  }


  public static boolean missing(@Environment EnvExp rho, SymbolExp symbol) {
    SEXP value = rho.findVariable(symbol);
    if(value == SymbolExp.UNBOUND_VALUE) {
      throw new EvalException("'missing' can only be used for arguments");

    } else {
      return value == SymbolExp.MISSING_ARG;
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
