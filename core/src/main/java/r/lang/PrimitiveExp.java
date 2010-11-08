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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class PrimitiveExp extends SEXP implements FunExp {

  protected final FunctionTable.Entry functionEntry;
  protected List<Method> methodOverloads;

  protected PrimitiveExp(FunctionTable.Entry functionEntry) {
    this.functionEntry = functionEntry;
  }

  public boolean isInternal() {
    return (functionEntry.eval % 100) / 10 != 0;
  }

  /**
   * @return a name for the function as a valid Java identifier
   */
  public Class getFunctionClass() {
    return functionEntry.functionClass;
  }

  public String getName() {
    return functionEntry.name;
  }

  @Override
  public EvalResult apply(LangExp call, PairList arguments, EnvExp rho) {
    checkArity(arguments);
    // PairList preparedArgs = prepareArguments(arguments, rho);

    List<Method> overloads = getMethodOverloads();
    if(overloads.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("'" + functionEntry.name + "' is not yet implemented");
      if(functionEntry.functionClass != null) {
        message.append(" (").append(functionEntry.functionClass.getName())
            .append(".").append(functionEntry.methodName);
      }
      throw new EvalException(message.toString());
    }

    return RuntimeInvoker.INSTANCE.invoke(rho, call,
        overloads);
  }

  public final void checkArity(PairList arguments) {

    if (functionEntry.arity >= 0 && functionEntry.arity != arguments.length()) {
      if (isInternal()) {
        throw new EvalException(this, "%d arguments passed to .Internal(%s) which requires %d",
            arguments.length(),
            functionEntry.name,
            functionEntry.arity);
      } else {
        throw new EvalException(this, "%d argument passed to '%s' which requires %d",
            arguments.length(),
            functionEntry.name,
            functionEntry.arity);
      }
    }
  }

  protected abstract PairList prepareArguments(PairList args, EnvExp rho);

  protected List<Method> getMethodOverloads() {
    if (methodOverloads == null) {
      methodOverloads = new ArrayList<Method>();
      if(functionEntry.functionClass != null) {
        for(Method method : functionEntry.functionClass.getMethods()) {
          if(Modifier.isPublic(method.getModifiers()) &&
              Modifier.isStatic(method.getModifiers()) &&
              method.getName().equals(functionEntry.methodName)) {

            methodOverloads.add(method);
          }
        }
      }
    }
    return methodOverloads;
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
