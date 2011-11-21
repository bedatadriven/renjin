package r.jvmi.r2j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import r.jvmi.r2j.converters.Converter;
import r.jvmi.r2j.converters.Converters;
import r.jvmi.wrapper.ArgumentIterator;
import r.lang.Context;
import r.lang.Environment;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.exception.EvalException;

import com.google.common.collect.Lists;

public class FunctionBinding {

  private List<Overload> overloads = Lists.newArrayList();
  private int maxArgCount;
  
  public FunctionBinding(Iterable<Method> overloads) {
    for(Method method : overloads) {
      Overload overload = new Overload(method);
      if(overload.getArgCount() > maxArgCount) {
        maxArgCount = overload.getArgCount();
      }
      this.overloads.add(overload);
    }
    sortOverloads();
  }
  
  public FunctionBinding(Method[] methods) {
    this(Arrays.asList(methods));
  }

  public static class Overload {
    private Method method;
    private int nargs;
    private Converter[] argumentConverters;
    private Converter returnValueConverter;
    
    public Overload(Method method) {
      this.method = method;
      this.nargs = method.getParameterTypes().length;
      this.argumentConverters = new Converter[method.getParameterTypes().length];
      for(int i=0;i!=argumentConverters.length;++i) {
        argumentConverters[i] = Converters.get(method.getParameterTypes()[i]);
      }
      this.returnValueConverter = Converters.get(method.getReturnType());    
    
      // workaround reflection problem calling 
      // public methods on private subclasses
      // see http://download.oracle.com/javase/tutorial/reflect/member/methodTrouble.html
      this.method.setAccessible(true);
    }
    
    public int getArgCount() {
      return nargs;
    }
    
    public boolean accept(SEXP[] args) {
      for(int i=0; i!=nargs;++i) {
        if(!argumentConverters[i].acceptsSEXP(args[i])) {
          return false;
        }
      }
      return true;
    }
    
    public SEXP invoke(Object instance, SEXP[] args) {
      Object converted[] = new Object[nargs];
      for(int i=0;i!=nargs;++i) {
        converted[i] = argumentConverters[i].convertToJava(args[i]);
      }
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
  }
  
  /**
   * Orders the list of overloads so that we try the most specific first 
   * (like boolean) before we try more general overloads (like string)
   */
  private void sortOverloads() {
    Collections.sort(overloads, new Comparator<Overload>() {

      @Override
      public int compare(Overload o1, Overload o2) {
        if(o1.getArgCount() != o2.getArgCount()) {
          return o1.getArgCount() - o2.getArgCount();
        }
        for(int i=0;i!=o1.getArgCount();++i) {
          int cmp = o1.argumentConverters[i].getSpecificity() - 
              o2.argumentConverters[i].getSpecificity();
          if(cmp != 0) {
            return cmp;
          }
        }
        return 0;
      }
      
    });
    
    
  }
  
  public SEXP invoke(Object instance, Context context, Environment rho, PairList arguments) {
    
    // eval arguments
    SEXP args[] = new SEXP[maxArgCount];
    ArgumentIterator it = new ArgumentIterator(context, rho, arguments);
    int nargs = 0;
    while(it.hasNext()) {
      args[nargs++] = it.next().evaluate(context, rho);
    }
    
    // find overload
    for(Overload overload : overloads) {
      if(overload.getArgCount() == nargs &&
          overload.accept(args)) {
        
        return overload.invoke(instance, args);
        
      }
    }
    throw new EvalException("Cannot match arguments (%s) to any JVM method overload:\n%s", toString(args), overloadListToString());
  }
  
  private String toString(SEXP[] args) {
    StringBuilder list = new StringBuilder();
    for(SEXP arg : args) {
      if(arg == null) {
        break;
      }
      if(list.length() > 0) {
        list.append(", ");
      }
      list.append(arg.getTypeName());
    }
    return list.toString();
  }
  
  private String overloadListToString() {
    StringBuilder sb = new StringBuilder();
    for(Overload overload : overloads) {
      sb.append("\n\t").append(overload.toString());
    }
    return sb.toString();
  }
  
}
