package org.renjin.compiler.pipeline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class DeferredJitCache {

  public static final DeferredJitCache INSTANCE = new DeferredJitCache();


  private final Cache<JitKey, JittedComputation> cache;


  private DeferredJitCache() {
    cache = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(100)
            .build();
  }

  public JittedComputation compile(DeferredNode node) {
    JitKey key = node.jitKey();
    JittedComputation computation = cache.getIfPresent(key);
    if(computation != null) {
      return computation;
    }
    DeferredJitter jitter = new DeferredJitter();
    computation = jitter.compile(node);
    cache.put(key, computation);

    return computation;
  }
}
