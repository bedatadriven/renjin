package r.jvmi.r2j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import r.jvmi.r2j.converters.Converter;
import r.jvmi.r2j.converters.Converters;
import r.lang.SEXP;
import r.lang.exception.EvalException;

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
  
  public static class Overload {
    private Constructor constructor;
    private int nargs;
    private Converter[] argumentConverters;
    
    public Overload(Constructor constructor) {
      this.constructor = constructor;
      this.nargs = constructor.getParameterTypes().length;
      this.argumentConverters = new Converter[constructor.getParameterTypes().length];
      for(int i=0;i!=argumentConverters.length;++i) {
        argumentConverters[i] = Converters.get(constructor.getParameterTypes()[i]);
      }
    }
    
    public int getArgCount() {
      return nargs;
    }
    
    public boolean accept(List<SEXP> args) {
      for(int i=0; i!=nargs;++i) {
        if(!argumentConverters[i].acceptsSEXP(args.get(i))) {
          return false;
        }
      }
      return true;
    }
    
    public Object newInstance(List<SEXP> args) {
      Object converted[] = new Object[nargs];
      for(int i=0;i!=nargs;++i) {
        converted[i] = argumentConverters[i].convertToJava(args.get(i));
      }
      try {
        return constructor.newInstance(converted);
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
  }
  
  public Object newInstance(List<SEXP> arguments) {
    for(Overload overload : overloads) {
      if(overload.getArgCount() == arguments.size() &&
          overload.accept(arguments)) {
        
        return overload.newInstance(arguments);
        
      }
    }
    throw new EvalException("cannt match actuals to jvm metod arguments");
  }
}
