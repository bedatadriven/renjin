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
package org.renjin.invoke.reflection;

import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.converters.Converter;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Defines a binding between a Java property with getters and setters and
 * a member of
 */
public class PropertyBinding implements MemberBinding {

  private Symbol name;
  private Method getter;
  private Converter getterConverter;

  private List<Setter> setters;

  public PropertyBinding(Symbol name, Method getter,
                         Collection<Method> setters) {
    this.name = name;
    this.getter = getter;
    this.getterConverter = Converters.get(getter.getReturnType());

    this.setters = Lists.newArrayList();
    for(Method setter : setters) {
      this.setters.add(new Setter(setter));
    }
  }

  public Symbol getName() {
    return name;
  }

  public Converter getConverter() {
    return getterConverter;
  }
  
  public SEXP getValue(Object instance) {
    try {
      return getterConverter.convertToR(getter.invoke(instance));
    } catch (Exception e) {
      throw new EvalException("Exception thrown while invoking getter '%s' on instance of class '%s'",
              getter.getName(), getter.getDeclaringClass().getName());
    }
  }
  
  public Object getRawValue(Object instance) {
    try {
      return getter.invoke(instance);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("IllegalAccessException thrown while accessing public member " + name, e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private static class Setter {
    private Method method;
    private Converter converter;

    public Setter(Method method) {
      this.method = method;
      this.converter = Converters.get(method.getParameterTypes()[0]);
    }

    public void setValue(Object instance, SEXP value) {
      Object convertedValue = converter.convertToJava(value);
      try {
        method.invoke(instance, convertedValue);
      } catch (Exception e) {
        throw new EvalException("Exception thrown while calling setter '%s' on instance of class '%s': %s",
                method.getName(), method.getDeclaringClass().getName(), e.getMessage());
      }
    }
  }

  @Override
  public void setValue(Object instance, SEXP value) {
    if(setters.size() == 0) {
      throw new EvalException("The property '%s' on class '%s' is read-only", name,
              getter.getDeclaringClass().getName());
    } else if(setters.size() > 1) {
      throw new EvalException("Overloaded setters are not yet implemented");
    }

    setters.get(0).setValue(instance, value);
  }
}
