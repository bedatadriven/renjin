package org.renjin.jvminterop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.jvminterop.converters.Converter;
import org.renjin.jvminterop.converters.Converters;
import org.renjin.primitives.annotations.Current;
import org.renjin.sexp.SEXP;


public abstract class AbstractOverload {
  private int nargs;
  
  /**
   * The number of non-varArgs arguments
   */
  private int baseArgCount;
  
  private boolean varArgs;
  private int firstArg = 0;
  private boolean context;
  private Converter[] argumentConverters;
  
  private Converter varArgConverter;
  private Converter varArgArrayConverter;
  private Class varArgElementClass;
  
  
  public AbstractOverload(Class[] parameterTypes, Annotation[][] annotations, boolean varArgs) {
    this.nargs = parameterTypes.length;
    this.varArgs = varArgs;
    
    if(varArgs) {
      baseArgCount = nargs - 1;
    } else {
      baseArgCount = nargs;
    }
    
    // check for @Current Context args
    if(firstArgIsContext(annotations)) {
      firstArg = 1;
      baseArgCount --;
      context = true;
    }
    
    this.argumentConverters = new Converter[baseArgCount];
    for(int i=0;i!=baseArgCount;++i) {
      argumentConverters[i] = Converters.get(parameterTypes[firstArg+i]);
    }
    
    if(varArgs) {
      varArgArrayConverter = Converters.get(parameterTypes[nargs-1]);
      varArgElementClass = parameterTypes[nargs-1].getComponentType();
      varArgConverter = Converters.get(varArgElementClass);
    }
  }

  private boolean firstArgIsContext(Annotation[][] annotations) {
    if(annotations.length == 0) {
      return false;
    }
    for(int i=0;i!=annotations[0].length;++i) {
      if(annotations[0][i] instanceof Current) {
        return true;
      }
    }
    return false;
  }
  
  protected final Object[] convertArguments(Context context, List<SEXP> args) {
    Object converted[] = new Object[nargs];
    if(this.context) {
      converted[0] = context;
    }
    for(int i=0;i!=baseArgCount;++i) {
      converted[i+firstArg] = argumentConverters[i].convertToJava(args.get(i));
    }
    if(varArgs) {
      int nVarArgs = args.size() - baseArgCount;
      if(nVarArgs == 1  && varArgArrayConverter.acceptsSEXP(args.get(baseArgCount))) {
        converted[nargs-1] = varArgArrayConverter.convertToJava(args.get(baseArgCount));
      } else {
        Object extra = Array.newInstance(varArgElementClass, args.size() - baseArgCount);
        for(int i=0; (i+baseArgCount)<args.size();++i) {
          Array.set(extra, i, varArgConverter.convertToJava(args.get(i+baseArgCount)));
        }
        converted[nargs-1] = extra;
      }
    }
    return converted;
  }
  
  public final int getArgCount() {
    return nargs;
  }
  
  public boolean isVarArgs() {
    return varArgs;
  }
  
  public boolean accept(List<SEXP> args) {
    if(args.size() < baseArgCount) {
      return false;
    }
    if(!varArgs && args.size() > baseArgCount) {
      return false;
    }
    for(int i=0; i!=baseArgCount;++i) {
      if(!argumentConverters[i].acceptsSEXP(args.get(i))) {
        return false;
      }
    }
    for(int i=baseArgCount;i<args.size();++i) {
      if(!varArgConverter.acceptsSEXP(args.get(i))) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Orders the list of overloads so that we try the most specific first 
   * (like boolean) before we try more general overloads (like string)
   */
  public static void sortOverloads(List<? extends AbstractOverload> overloads) {
    Collections.sort(overloads, new Comparator<AbstractOverload>() {

      @Override
      public int compare(AbstractOverload o1, AbstractOverload o2) {
        if(o1.baseArgCount != o2.baseArgCount) {
          return o1.baseArgCount - o2.baseArgCount;
        }
        for(int i=0;i!=o1.baseArgCount;++i) {
          int cmp = o1.argumentConverters[i].getSpecificity() - 
              o2.argumentConverters[i].getSpecificity();
          if(cmp != 0) {
            return cmp;
          }
        }
        if(!o1.varArgs && !o2.varArgs) {
          return 0;
        }
        if(o1.varArgs && !o2.varArgs) {
          return 1;
        } 
        if(!o1.varArgs && o2.varArgs) {
          return -1;
        }
        
        return o1.varArgConverter.getSpecificity() - 
              o2.varArgConverter.getSpecificity();
        
      }
    });
  }
}
