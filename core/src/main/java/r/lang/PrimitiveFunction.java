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
import r.lang.exception.FunctionCallException;
import r.lang.primitive.BaseFrame;
import r.lang.primitive.binding.PrimitiveMethod;
import r.lang.primitive.binding.RuntimeInvoker;

import java.util.List;

public abstract class PrimitiveFunction extends AbstractSEXP implements Function {

  public static final String IMPLICIT_CLASS = "function";

  protected final BaseFrame.Entry functionEntry;
  protected List<PrimitiveMethod> methodOverloads;
  private String name;

  protected PrimitiveFunction(BaseFrame.Entry functionEntry) {
    this.functionEntry = functionEntry;
    name = this.functionEntry.name;
  }

  @Override
  protected final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  public String getName() {
    return functionEntry.name;
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList arguments) {
    List<PrimitiveMethod> overloads = getMethodOverloads(functionEntry.functionClass, name, functionEntry.methodName);
    if(overloads.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("'")
             .append(name)
             .append("' is not yet implemented");
      if(functionEntry.functionClass != null) {
        message.append(" (")
             .append(functionEntry.functionClass.getName())
             .append(".")
             .append(functionEntry.methodName)
             .append(")");
      }
      throw new EvalException(message.toString());
    }

    try {
      return RuntimeInvoker.INSTANCE.invoke(context, rho, call, overloads);
    } catch (EvalException e) {
      throw new FunctionCallException(call, arguments, e);
    }
  }

  protected List<PrimitiveMethod> getMethodOverloads(Class clazz, String name, String alias) {
    if (methodOverloads == null) {
      methodOverloads = PrimitiveMethod.findOverloads(clazz, name, alias);
    }
    return methodOverloads;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return name + "()";
  }
}
