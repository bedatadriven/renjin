package org.renjin.methods;

import org.renjin.repackaged.guava.cache.CacheBuilder;
import org.renjin.repackaged.guava.cache.CacheLoader;
import org.renjin.repackaged.guava.cache.LoadingCache;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Null;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;

import java.util.concurrent.ExecutionException;

public class PrimitiveMethodTable {

  public enum prim_methods_t {NO_METHODS, NEEDS_RESET, HAS_METHODS, SUPPRESSED} ;

  public static class Entry {

    private prim_methods_t methods;
    private Closure generic;
    private SEXP methodList = Null.INSTANCE;
    
    public void setMethods(prim_methods_t code) {
      this.methods = code;
    }

    
    public void setGeneric(Closure fundef) {
      this.generic = fundef;
    }

    public void setMethodList(SEXP methodList) {
      this.methodList = methodList;
    }
    
  }

  private LoadingCache<PrimitiveFunction, Entry> map;
  private boolean primitiveMethodsAllowed = false;
  
  public PrimitiveMethodTable() {
    this.map = CacheBuilder.newBuilder()
        .build(new CacheLoader<PrimitiveFunction, Entry>() {

          @Override
          public Entry load(PrimitiveFunction key) throws Exception {
            return new Entry();
          }
        });
  }
      
  public Entry get(PrimitiveFunction fn) {
    try {
      return map.get(fn);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isPrimitiveMethodsAllowed() {
    return primitiveMethodsAllowed;
  }

  public void setPrimitiveMethodsAllowed(boolean primitiveMethodsAllowed) {
    this.primitiveMethodsAllowed = primitiveMethodsAllowed;
  }
 
}
