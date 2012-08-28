package org.renjin.compiler.pipeline;

/**
 * A Just-in-time compiler for a specific function.
 */
public interface FunctionJitter {
  void compute(ComputeMethod method, DeferredNode node);
}
