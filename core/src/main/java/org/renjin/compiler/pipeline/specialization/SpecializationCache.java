package org.renjin.compiler.pipeline.specialization;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.renjin.compiler.pipeline.DeferredNode;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class SpecializationCache {

  public static final SpecializationCache INSTANCE = new SpecializationCache();

  private final Cache<SpecializationKey, SpecializedComputation> cache;

  private SpecializationCache() {
    cache = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(100)
            .build();
  }

  public SpecializedComputation compile(DeferredNode node) {
    SpecializationKey key = node.jitKey();
    try {
      SpecializedComputation computation = cache.getIfPresent(key);
      if (computation != null) {
        return computation;
      }
      JitSpecializer jitter = new JitSpecializer();
      computation = jitter.compile(node);
      cache.put(key, computation);

      return computation;
    } catch (Exception e) {
      throw new RuntimeException("Failed to compile " + key, e);
    }
  }
}
