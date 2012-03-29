package org.renjin.jvminterop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.renjin.eval.EvalException;
import org.renjin.jvminterop.converters.Converter;
import org.renjin.jvminterop.converters.Converters;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


import com.google.common.collect.Lists;

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
  
  public SEXP getValue(Object instance) {
    try {
      return getterConverter.convertToR(getter.invoke(instance));
    } catch (IllegalArgumentException e) {
      // shouldn't happen, we're not passing arguments to the getter!
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      // shouldn't happen, we've already checked that the method is public
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
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
      try {
        method.invoke(instance, converter.convertToJava(value));
      } catch (IllegalArgumentException e) {
        // should not happen since we're handling the conversion
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        // should not happen since we've already made sure the setter is public
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new EvalException("Exception thrown while calling setter '%s' on instance of class '%s'", 
            method.getName(), method.getDeclaringClass().getName());
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
