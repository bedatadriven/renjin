package org.renjin.compiler.pipeline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.renjin.compiler.pipeline.opencl.KernelComputation;
import org.renjin.compiler.pipeline.opencl.KernelProvider;
import org.renjin.compiler.pipeline.opencl.MeanKernel;
import org.renjin.compiler.pipeline.opencl.RowMeanKernel;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class DeferredJitCache {

  public static final DeferredJitCache INSTANCE = new DeferredJitCache();

  private final Cache<JitKey, JittedComputation> cache;
  private boolean OPEN_CL_ENABLED = false;

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

    computation = doCompilation(node);
    cache.put(key, computation);

    return computation;
  }

  private JittedComputation doCompilation(DeferredNode node) {
    if(OPEN_CL_ENABLED) {
      if(node.getComputation().getComputationName().equals("rowMeans")) {
        KernelProvider kernel = new RowMeanKernel(node);
        return new KernelComputation(kernel);
      }
      else if(node.getComputation().getComputationName().equals("mean")) {
        KernelProvider kernel = new MeanKernel(node);
        return new KernelComputation(kernel);
      }
    }
    DeferredJitter jitter = new DeferredJitter();
    return jitter.compile(node);
  }
}
