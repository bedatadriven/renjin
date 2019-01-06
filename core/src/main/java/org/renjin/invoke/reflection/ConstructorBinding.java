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
package org.renjin.invoke.reflection;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.SEXP;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

public class ConstructorBinding implements MemberBinding {

  private List<Overload> overloads = Lists.newArrayList();
  private int maxArgCount;
  
  public ConstructorBinding(Constructor[] overloads) {
    for(Constructor constructor : overloads) {
      if((constructor.getModifiers() & Modifier.PUBLIC) != 0) {
        Overload overload = new Overload(constructor);
        if(overload.getArgCount() > maxArgCount) {
          maxArgCount = overload.getArgCount();
        }
        this.overloads.add(overload);
      }
    }
    AbstractOverload.sortOverloads(this.overloads);
  }
  
  public boolean isEmpty() {
    return overloads.isEmpty();
  }

  @Override
  public SEXP getValue(Object instance) {
    return new ConstructorFunction(this);
  }

  @Override
  public void setValue(Object instance, SEXP value) {
    throw new EvalException("The constructor of JVM classes cannot be changed");
  }

  public static class Overload extends AbstractOverload {
    private Constructor constructor;

    
    public Overload(Constructor constructor) {
      super(constructor.getParameterTypes(), 
          constructor.getParameterAnnotations(), constructor.isVarArgs());
      this.constructor = constructor;
    }
    
   
    public Object newInstance(Context context, List<SEXP> args) {
      try {
        return constructor.newInstance(convertArguments(context, args));
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }  
    }
    
    @Override
    public String toString() {
      return constructor.toString();
    }
  }
  
  public Object newInstance(Context context, List<SEXP> arguments) {
    for(Overload overload : overloads) {
      if(overload.accept(arguments)) {
        
        return overload.newInstance(context, arguments);
        
      }
    }

    throw new EvalException("Cannot match arguments (%s) to any of the constructors:\n%s", 
        ExceptionUtil.toString(arguments), ExceptionUtil.overloadListToString(overloads));
  }
}
