package r.jvmi.r2j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import r.jvmi.r2j.converters.Converter;
import r.jvmi.r2j.converters.Converters;
import r.lang.SEXP;
import r.lang.exception.EvalException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ConstructorBinding {

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
  }
  
  public boolean isEmpty() {
    return overloads.isEmpty();
  }
  
  public static class Overload extends AbstractOverload {
    private Constructor constructor;

    
    public Overload(Constructor constructor) {
      super(constructor.getParameterTypes(), constructor.isVarArgs());
      this.constructor = constructor;
    }
    
   
    public Object newInstance(List<SEXP> args) {
      try {
        return constructor.newInstance(convertArguments(args));
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
  
  public Object newInstance(List<SEXP> arguments) {
    for(Overload overload : overloads) {
      if(overload.accept(arguments)) {
        
        return overload.newInstance(arguments);
        
      }
    }

    throw new EvalException("Cannot match arguments (%s) to any of the constructors:\n%s", 
        ExceptionUtil.toString(arguments), ExceptionUtil.overloadListToString(overloads));
  }
}
