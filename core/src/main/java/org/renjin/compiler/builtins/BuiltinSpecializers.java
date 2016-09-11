package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.cache.CacheBuilder;
import org.renjin.repackaged.guava.cache.CacheLoader;
import org.renjin.repackaged.guava.cache.LoadingCache;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;
import java.util.concurrent.ExecutionException;


public class BuiltinSpecializers {

  public static final BuiltinSpecializers INSTANCE = new BuiltinSpecializers();


  /**
   * For a few builtins, we have extra-special specializers.
   */
  private final Map<Primitives.Entry, Specializer> specializers = Maps.newHashMap();

  
  /**
   * For most builtins, specialization is done automatically using @Annotations present
   * in the code base. To avoid rebuilding the metadata each time one is needed, 
   * cache instances of BuiltinSpecializer here.
   */
  private final LoadingCache<Primitives.Entry, BuiltinSpecializer> cache;
  
  
  public BuiltinSpecializers() {
    
    specializers.put(Primitives.getBuiltinEntry("length"), new GenericBuiltinGuard(new LengthSpecializer()));
    specializers.put(Primitives.getBuiltinEntry("[<-"), new GenericBuiltinGuard(new ReplaceSpecializer()));
    cache = CacheBuilder.newBuilder().build(new CacheLoader<Primitives.Entry, BuiltinSpecializer>() {
      @Override
      public BuiltinSpecializer load(Primitives.Entry entry) throws Exception {
        return new BuiltinSpecializer(entry);
      }
    });
  }
  
  public Specializer get(Primitives.Entry primitive) {
    Preconditions.checkNotNull(primitive);
    
    if(specializers.containsKey(primitive)) {
      return specializers.get(primitive);
    }
    
    try {
      return cache.get(primitive);
    } catch (ExecutionException e) {
      throw new InternalCompilerException(e);
    }
  }
}
