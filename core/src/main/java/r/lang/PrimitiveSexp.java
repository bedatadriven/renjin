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
import r.lang.primitive.FunctionTable;
import r.lang.primitive.PrimitiveFunction;

public abstract class PrimitiveSexp extends SEXP implements FunExp {

  protected final FunctionTable.Entry functionEntry;
  protected PrimitiveFunction functionInstance;

  protected PrimitiveSexp(FunctionTable.Entry functionEntry) {
    this.functionEntry = functionEntry;
  }

  public boolean isInternal() {
    return (functionEntry.eval % 100) / 10 != 0;
  }

  public int getArity() {
    return functionEntry.arity;
  }

  /**
   * @return a name for the function as a valid Java identifier
   */
  public Class getFunctionClass() {
    return functionEntry.functionClass;
  }

  @Override
  public EvalResult apply(LangExp call, NillOrListExp args, EnvExp rho) {
    checkArity(args);
    NillOrListExp preparedArgs = prepareArguments(args, rho);

    return getFunctionInstance().apply(call, rho, preparedArgs);
  }

  public final void checkArity(NillOrListExp args) {

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

  protected abstract NillOrListExp prepareArguments(NillOrListExp args, EnvExp rho);

  protected PrimitiveFunction getFunctionInstance() {
    if (functionInstance == null) {

      try {
        functionInstance = (PrimitiveFunction) functionEntry.functionClass.newInstance();
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
