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

package r.lang;

import r.lang.exception.EvalException;
import r.lang.primitive.BuiltinFunction;
import r.lang.primitive.FunctionTable;

public abstract class PrimitiveSexp extends SEXP implements FunSxp {

  protected final FunctionTable.Entry functionEntry;
  protected BuiltinFunction functionInstance;

  protected PrimitiveSexp(FunctionTable.Entry functionEntry) {
    this.functionEntry = functionEntry;
  }

  public boolean isInternal() {
    return (functionEntry.eval % 100) / 10 != 0;
  }

  public int getArity() {
    return functionEntry.arity;
  }

  @Override
  public SEXP apply(LangExp call, ListExp args, EnvExp rho) {
    checkArity(args);
    // "builtin" functions have their arguments evaluated before being passed in
    return getFunctionInstance().apply(call, evaluateArgs(args, rho), rho);
  }

  public final void checkArity(ListExp args) {

    if (functionEntry.arity >= 0 && functionEntry.arity != args.length()) {
      if (isInternal()) {
        throw new EvalException(this, "%d arguments passed to .Internal(%s) which requires %d",
            args.length(),
            functionEntry.name,
            functionEntry.arity);
      } else {
        throw new EvalException(this, "%d argument passed to '%s' which requires %d",
            args.length(),
            functionEntry.name,
            functionEntry.arity);
      }
    }
  }

  protected ListExp evaluateArgs(ListExp args, EnvExp rho) {
    if (args == null) {
      return null;
    }
    ListExp.Builder builder = new ListExp.Builder();
    for (SEXP arg : args) {
      builder.add(arg.evaluate(rho));
    }
    return builder.list();
  }

  protected BuiltinFunction getFunctionInstance() {
    if (functionInstance == null) {

      try {
        functionInstance = (BuiltinFunction) functionEntry.functionClass.newInstance();
      } catch (Exception e) {
        throw new EvalException(this, e, "Could not create function class for function '" +
            functionEntry.name + "' (" + functionEntry.functionClass + "); it " +
            "may not yet be implemented.");
      }
    }
    return functionInstance;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);

  }

  @Override
  public String toString() {
    return functionEntry.name + "()";
  }
}
