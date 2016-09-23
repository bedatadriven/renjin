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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.invoke.reflection.converters.Converter;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Encapsulates the binding between a JVM method and a R function, including any
 * required conversions.
 */
public class FunctionBinding {

  private List<Overload> overloads = Lists.newArrayList();
  private int maxArgCount;
  
  public FunctionBinding(Iterable<Method> overloads) {
    for(Method method : overloads) {
      addOverload(method);
    }
    AbstractOverload.sortOverloads(this.overloads);
  }

  /**
   *
   * @return  the JVM classes on which this method was declared
   */
  public Class getDeclaringClass() {
    return Iterables.get(overloads, 0).getDeclaringClass();
  }

  public List<Overload> getOverloads() {
    return overloads;
  }

  /**
   * @return the name of the JVM method
   */
  public String getName() {
    return Iterables.get(overloads, 0).getName();
  }
  
  private void addOverload(Method method) {
    Overload overload = new Overload(method);
    if(overload.getArgCount() > maxArgCount) {
      maxArgCount = overload.getArgCount();
    }
    this.overloads.add(overload);
  }

  public static class Overload extends AbstractOverload {
    private Method method;
    private Converter returnValueConverter;
    
    public Overload(Method method) {
      super(method.getParameterTypes(),
            method.getParameterAnnotations(), 
            method.isVarArgs());
      this.method = method;
      this.returnValueConverter = Converters.get(method.getReturnType());    
    
      // workaround reflection problem calling 
      // public methods on private subclasses
      // see http://download.oracle.com/javase/tutorial/reflect/member/methodTrouble.html
      if(!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
        try {
          this.method.setAccessible(true);
        } catch (SecurityException ignored) {
          // if the security settings prevent us from accessing this class via reflection,
          // there may be a RuntimeException later on if there is an attempt to invoke this method,
          // but we should still continue constructing the metadata for this class.
        }
      }
    }
    
    public Class getDeclaringClass() {
      return method.getDeclaringClass();
    }
    
    public String getName() {
      return method.getName();
    }
    
    public SEXP invoke(Context context, Object instance, List<SEXP> args) {
      Object[] converted = convertArguments(context, args);
      try {
        Object result = method.invoke(instance, converted);
        return returnValueConverter.convertToR(result);
        
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Exception invoking " + method, e);
      } catch (InvocationTargetException e) {
        if(e.getCause() instanceof RuntimeException) {
          throw (RuntimeException)e.getCause();
        } else {
          throw new RuntimeException(e);
        }
      }
    }
    
    @Override
    public String toString() {
      return method.toString();
    }

    public Method getMethod() {
      return method;
    }
  }

  /**
   *
   * @param instance the JVM object instance
   * @param context the calling context
   * @param rho the calling environment
   * @param arguments the UNEVALUATED arguments
   */
  public SEXP evaluateArgsAndInvoke(Object instance, Context context, Environment rho, PairList arguments) {
    
    // eval arguments
    List<SEXP> args = Lists.newArrayListWithCapacity(maxArgCount);
    ArgumentIterator it = new ArgumentIterator(context, rho, arguments);
    while(it.hasNext()) {
      args.add(context.evaluate( it.next(), rho));
    }
    return invoke(instance, context, args);
  }

  /**
   *
   * @param instance the JVM object instance
   * @param context the calling context
   * @param evaluatedArguments  the already EVALUATED arguments
   */
  public SEXP invoke(Object instance, Context context, ListVector evaluatedArguments) {
    List<SEXP> args = Lists.newArrayList(evaluatedArguments);
    return invoke(instance, context, args);
  }

  private SEXP invoke(Object instance, Context context, List<SEXP> args) {
    // find overload
    for(Overload overload : overloads) {
      if(overload.accept(args)) {
        return overload.invoke(context, instance, args);
      }
    }
    throw new EvalException("Cannot match arguments (%s) to any JVM method overload:\n%s",
        ExceptionUtil.toString(args), ExceptionUtil.overloadListToString(overloads));
  }

  @Override
  public String toString() {
    return getName();
  }
}
