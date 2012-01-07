package r.jvmi.r2j;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import r.jvmi.r2j.converters.Converter;
import r.jvmi.r2j.converters.Converters;
import r.lang.SEXP;

public abstract class AbstractOverload {
  private int nargs;
  
  /**
   * The number of non-varArgs arguments
   */
  private int baseArgCount;
  
  private boolean varArgs;
  private Converter[] argumentConverters;
  
  private Converter varArgConverter;
  private Class varArgElementClass;
  
  public AbstractOverload(Class[] parameterTypes, boolean varArgs) {
    this.nargs = parameterTypes.length;
    this.varArgs = varArgs;
    
    if(varArgs) {
      baseArgCount = nargs - 1;
    } else {
      baseArgCount = nargs;
    }
    
    this.argumentConverters = new Converter[baseArgCount];
    for(int i=0;i!=baseArgCount;++i) {
      argumentConverters[i] = Converters.get(parameterTypes[i]);
    }
    
    if(varArgs) {
      varArgElementClass = parameterTypes[nargs-1].getComponentType();
      varArgConverter = Converters.get(varArgElementClass);
    }
  }
  
  protected final Object[] convertArguments(List<SEXP> args) {
    Object converted[] = new Object[nargs];
    for(int i=0;i!=baseArgCount;++i) {
      converted[i] = argumentConverters[i].convertToJava(args.get(i));
    }
    if(varArgs) {
      Object extra = Array.newInstance(varArgElementClass, args.size() - baseArgCount);
      for(int i=0; (i+baseArgCount)<args.size();++i) {
        Array.set(extra, i, varArgConverter.convertToJava(args.get(i+baseArgCount)));
      }
      converted[nargs-1] = extra;
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
