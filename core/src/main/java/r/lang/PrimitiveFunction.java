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

import r.base.BaseFrame;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.RuntimeInvoker;
import r.lang.exception.EvalException;
import r.lang.exception.FunctionCallException;

import java.util.List;

public abstract class PrimitiveFunction extends AbstractSEXP implements Function {

  public static final String IMPLICIT_CLASS = "function";

  protected List<JvmMethod> methodOverloads;
  private String name;
  private Class methodClass;
  private String methodName;

  protected PrimitiveFunction(BaseFrame.Entry functionEntry) {
    name = functionEntry.name;
    methodClass = functionEntry.functionClass;
    methodName = functionEntry.methodName;
  }

  protected PrimitiveFunction(String name, Class methodClass, String methodName) {
    this.name = name;
    this.methodClass = methodClass;
    this.methodName = methodName;
  }

  protected PrimitiveFunction(String name, Class methodClass) {
    this.name = name;
    this.methodClass = methodClass;
  }

  @Override
  protected final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  public String getName() {
    return name;
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList arguments) {

    List<JvmMethod> overloads = getMethodOverloads(methodClass, name, methodName);
    if(overloads.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("'")
             .append(name)
             .append("' is not yet implemented");
      if(methodClass != null) {
        message.append(" (")
             .append(methodClass.getName())
             .append(".")
             .append(methodName)
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

  protected List<JvmMethod> getMethodOverloads(Class clazz, String name, String alias) {
    if (methodOverloads == null) {
      methodOverloads = JvmMethod.findOverloads(clazz, name, alias);
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
