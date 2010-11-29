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

import com.google.common.collect.Lists;
import r.lang.exception.EvalException;
import r.lang.exception.FunctionCallException;
import r.lang.primitive.FunctionTable;
import r.lang.primitive.annotations.Primitive;
import r.lang.primitive.binding.PrimitiveMethod;
import r.lang.primitive.binding.RuntimeInvoker;

import java.lang.reflect.Method;
import java.util.List;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public abstract class PrimitiveExp extends SEXP implements FunExp {

  public static final String IMPLICIT_CLASS = "function";

  protected final FunctionTable.Entry functionEntry;
  protected List<PrimitiveMethod> methodOverloads;

  protected PrimitiveExp(FunctionTable.Entry functionEntry) {
    this.functionEntry = functionEntry;
  }

  public boolean isInternal() {
    return (functionEntry.eval % 100) / 10 != 0;
  }

  @Override
  protected final String getImplicitClass() {
    return IMPLICIT_CLASS;
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
    List<PrimitiveMethod> overloads = getMethodOverloads();
    if(overloads.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("'" + functionEntry.name + "' is not yet implemented");
      if(functionEntry.functionClass != null) {
        message.append(" (").append(functionEntry.functionClass.getName())
            .append(".").append(functionEntry.methodName).append(")");
      }
      throw new EvalException(message.toString());
    }

    try {
      return RuntimeInvoker.INSTANCE.invoke(rho, call, overloads);
    } catch (EvalException e) {
      throw new FunctionCallException(call, e);
    }
  }

  protected List<PrimitiveMethod> getMethodOverloads() {
    if (methodOverloads == null) {
      methodOverloads = Lists.newArrayList();
      if(functionEntry.functionClass != null) {
        for(Method method : functionEntry.functionClass.getMethods()) {

          if(isPublic(method.getModifiers()) &&
             isStatic(method.getModifiers()) &&
              method.getName().equals(functionEntry.methodName) ||
              alias(method).equals(functionEntry.name) )
          {
            methodOverloads.add(new PrimitiveMethod(method));
          }
        }
      }
      PrimitiveMethod.validate(methodOverloads);
    }
    return methodOverloads;
  }

  private String alias(Method method) {
    Primitive alias = method.getAnnotation(Primitive.class);
    return alias == null ? "" : alias.value();
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
