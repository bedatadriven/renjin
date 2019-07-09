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
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;
import org.renjin.invoke.annotations.CompilerSpecialization;

import java.util.*;

public final class FunctionEnvironment extends Environment {


  /**
   * An array of Symbols that correspond to the names of the local variables stored in {@link #locals}.
   *
   * Names of formal arguments are stored at the beginning. The rest of the array contains names of local variables
   * that were found at compile time. In compiled code, these variables can be refered to by index rather than name,
   * sparing the expense of a hash lookup.
   */
  private final SEXP[] localNames;


  /**
   * An array of arguments passed to this function. Missing arguments will be {@code null} in the array, even if they
   * have default values. Default values of arrays are assigned into the {@link #locals} array.
   */
  private final SEXP[] matchedArguments;

  private final MatchedArguments matching;
  private final SEXP[] locals;

  private final DispatchTable dispatchTable;

  /**
   * A map to store bindings of variables not identified at compile time. It is only allocated if needed.
   */
  private IdentityHashMap<Symbol, SEXP> overflow = null;


  public FunctionEnvironment(Environment parent, SEXP[] localNames, SEXP[] matchedArguments, MatchedArguments matching, SEXP[] locals, DispatchTable dispatch) {
    super(parent, null, AttributeMap.EMPTY);
    this.localNames = localNames;
    this.matchedArguments = matchedArguments;
    this.matching = matching;
    this.locals = locals;
    this.dispatchTable = dispatch;
  }

  @CompilerSpecialization
  public boolean isMissingArgument(Context context, String symbol) {
    return isMissingArgument(context, Symbol.get(symbol));
  }

  @CompilerSpecialization
  public boolean isMissingArgument(Context context, int i) {
    if(matchedArguments[i] == null) {
      return true;
    }
    Symbol symbol = (Symbol)localNames[i];
    if(symbol == Symbols.ELLIPSES) {
      return matchedArguments[i].length() == 0;
    }

    if(symbol.isVarArgReference()) {
      return isVarArgMissing(context, symbol.getVarArgReferenceIndex());
    }

    // Otherwise need to lookup symbol further up...
    return isArgMissing(context, this, symbol);
  }

  public boolean isMissingArgument(Context context, Symbol symbol) {

    for (int i = 0; i < matchedArguments.length; i++) {
      if(localNames[i] == symbol) {
        if(matchedArguments[i] == null) {
          return true;
        }
        if(localNames[i] == Symbols.ELLIPSES) {
          return matchedArguments[i].length() == 0;
        }
      }
    }

    if(symbol.isVarArgReference()) {
      return isVarArgMissing(context, symbol.getVarArgReferenceIndex());

    } else if(symbol == Symbols.ELLIPSES) {
      throw new EvalException("this function does not have a '...' formal argument");
    }

    // Otherwise need to lookup symbol further up...
    return isArgMissing(context, this, symbol);
  }


  /**
   * The '..1' argument is considered to be "missing" IF one is not provided, OR it is a promise
   * to a missing argument with no default value.
   */
  private boolean isVarArgMissing(Context context, int varArgReferenceIndex) {

    int ellipsesIndex = indexOf(Symbols.ELLIPSES);
    if(ellipsesIndex == -1) {
      throw new EvalException("missing can only be used for arguments.");
    }
    PromisePairList expando = (PromisePairList) matchedArguments[ellipsesIndex];
    if(expando.length() < varArgReferenceIndex) {
      return true;
    }
    SEXP value = expando.getElementAsSEXP(varArgReferenceIndex-1);
    return value == Symbol.MISSING_ARG || isPromisedMissingArg(context, value, new ArrayDeque<>());
  }

  private static boolean isArgMissing(Context context, Environment rho, Symbol argumentName) {

    SEXP value = rho.findVariable(context, argumentName);

    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("missing can only be used for arguments.");
    }
    if (value == Symbol.MISSING_ARG) {
      return true;
    }

    return isPromisedMissingArg(context, value, new ArrayDeque<>());
  }

  /**
   * @return true if {@code exp} evaluates to a missing argument with no default value.
   */
  private static boolean isPromisedMissingArg(Context context, SEXP exp, ArrayDeque<Promise> stack) {
    if(exp instanceof Promise) {
      Promise promise = (Promise)exp;

      if(promise.getPromisedExpression() instanceof Symbol) {

        // Avoid infinite recursion in the case of circular references, for example:
        // g <- function(x, y) { missing(x) }
        // f <- function(x = y, y = x) { g(x, y) }
        // f()
        if(stack.contains(promise)) {
          return true;
        }

        stack.push(promise);
        try {
          SEXP argumentValue;
          Symbol argumentName = (Symbol) promise.getPromisedExpression();
          Environment argumentEnv = promise.getEnvironment();

          if(argumentName.isVarArgReference()) {
            SEXP forwardedArguments = argumentEnv.findVariable(context, Symbols.ELLIPSES);
            if(forwardedArguments.length() < argumentName.getVarArgReferenceIndex()) {
              return true;
            }
            argumentValue = forwardedArguments.getElementAsSEXP(argumentName.getVarArgReferenceIndex() - 1);
          } else {
            argumentValue = argumentEnv.findVariable(context, argumentName);
          }

          if (argumentValue == Symbol.MISSING_ARG) {
            return true;
          } else if (isPromisedMissingArg(context, argumentValue, stack)) {
            return true;
          }
        } finally {
          stack.pop();
        }
      }
    }
    return false;
  }

  public void set(int index, SEXP value) {
    locals[index] = value;
    if(index < matchedArguments.length) {
      matchedArguments[index] = value;
    }
  }

  public SEXP getPromised(int index) {
    return locals[index];
  }


  /**
   * Access an index frame variable from the function environment, searching parent
   * environments if necessary.
   *
   * <p>If no binding is found, an EvalException is thrown.</p>
   * @param index the index of the variable, assigned at compile time.
   * @return the bound value
   * @throws EvalException if there is no binding
   */
  public SEXP get(Context context, int index) {
    SEXP value = locals[index];
    if(value != null) {
      return value.force(context);
    }
    Symbol symbol = (Symbol) localNames[index];

    if(overflow != null) {
      value = overflow.get(symbol);
      if(value != null) {
        return value.force(context);
      }
    }

    if(symbol.isDispatchMetadata() && dispatchTable != null) {
      return dispatchTable.get(symbol);
    }

    value = getParent().findVariable(context, symbol);

    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + symbol + "' not found.");
    }
    return value.force(context);
  }

  private int indexOf(Symbol name) {
    for (int i = 0; i < localNames.length; i++) {
      if (localNames[i] == name) {
        return i;
      }
    }
    return -1;
  }

  @Override
  protected Collection<Symbol> listBindings() {
    Set<Symbol> symbols = new HashSet<>();
    for (int i = 0; i < localNames.length; i++) {
      if(locals[i] != null) {
        symbols.add((Symbol)localNames[i]);
      }
    }
    if(overflow != null) {
      symbols.addAll(overflow.keySet());
    }
    if(dispatchTable != null) {
      symbols.addAll(dispatchTable.getEnvironmentSymbols());
    }

    return symbols;
  }

  @Override
  protected boolean isBound(Symbol symbol) {
    int localIndex = indexOf(symbol);
    if(localIndex >= 0) {
      return locals[localIndex] != null;
    } else {
      if(overflow != null) {
        return overflow.containsKey(symbol);
      }
    }

    if(symbol.isDispatchMetadata()) {
      throw new UnsupportedOperationException("TODO");
    }
    return false;
  }

  @Override
  protected SEXP getBinding(Symbol symbol) {
    int localIndex = indexOf(symbol);
    if(localIndex >= 0) {
      SEXP value = locals[localIndex];
      if(value != null) {
        return value;
      }
    } else if(overflow != null) {
      SEXP value = overflow.get(symbol);
      if(value != null) {
        return value;
      }
    }

    if(symbol.isDispatchMetadata() && dispatchTable != null) {
      return dispatchTable.get(symbol);
    }

    return Symbol.UNBOUND_VALUE;
  }

  @Override
  protected Function getFunctionBinding(Context context, Symbol symbol) {
    int localIndex = indexOf(symbol);

    SEXP value = null;

    if(localIndex >= 0) {
      value = locals[localIndex];

      // Special case: if the symbol matches a missing argument with no default, we
      // throw an error
      if(value == null && localIndex < matchedArguments.length) {
        throw new EvalException("argument \"%s\" is missing, with no default", symbol.getPrintName());
      }
    } else if(overflow != null) {
      value = overflow.get(symbol);
    }

    if(value == null) {
      return null;
    }

    if(value == Symbol.MISSING_ARG) {
      throw new EvalException("argument \"%s\" is missing, with no default", symbol.getPrintName());
    }

    if(value instanceof Promise) {
      value = value.force(context);
    }

    if(value instanceof Function) {
      return (Function)value;
    }

    return null;
  }

  @Override
  protected void removeBinding(Symbol symbol) {
    int localIndex = indexOf(symbol);
    if(localIndex >= 0) {
      locals[localIndex] = null;
    } else if(overflow != null) {
      overflow.remove(symbol);
    }
  }

  @Override
  protected void updateBinding(Symbol symbol, SEXP value) {
    int localIndex = indexOf(symbol);
    if(localIndex >= 0) {
      locals[localIndex] = value;

      // Clear the missing argument flag if neccessary
      if(localIndex < matchedArguments.length) {
        matchedArguments[localIndex] = value;
      }

    } else {
      if(overflow == null) {
        overflow = new IdentityHashMap<>();
      }
      overflow.put(symbol, value);
    }
  }

  public DispatchTable getDispatchTable() {
    return dispatchTable;
  }

  public int getFormalCount() {
    return matchedArguments.length;
  }

  /**
   * Returns the promised argument matched to the first formal argument, or {@code null} if no value
   * was provided for the first formal.
   */
  public SEXP getFormalValue(int index) {
    return matchedArguments[index];
  }

  public Symbol getLocalName(int index) {
    return (Symbol)localNames[index];
  }

  /**
   *
   * @return the matching between the arguments as passed by the caller and this closure's formal arguments.
   */
  public MatchedArguments getMatching() {
    return matching;
  }

  public SEXP promise(int varIndex) {
    return new Promise(this, localNames[varIndex]) {
      @Override
      protected SEXP doEval(Context context) {
        return get(context, varIndex);
      }
    };
  }

  public SEXP promise(String symbol) {
    return new Promise(this, Symbol.get(symbol));
  }
}
