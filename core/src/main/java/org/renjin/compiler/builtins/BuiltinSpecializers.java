package org.renjin.compiler.builtins;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.primitives.Primitives;

import java.util.concurrent.ExecutionException;


public class BuiltinSpecializers {

  public static final BuiltinSpecializers INSTANCE = new BuiltinSpecializers();
  
  
  private final LoadingCache<String, BuiltinSpecializer> cache;

  public BuiltinSpecializers() {
    cache = CacheBuilder.newBuilder().build(new CacheLoader<String, BuiltinSpecializer>() {
      @Override
      public BuiltinSpecializer load(String key) throws Exception {
        return new BuiltinSpecializer(Primitives.getBuiltinEntry(key));
      }
    });
  }
  
  public Specializer get(Primitives.Entry primitive) {
    try {
      return cache.get(primitive.name);
    } catch (ExecutionException e) {
      throw new InternalCompilerException(e);
    }
  }
}
