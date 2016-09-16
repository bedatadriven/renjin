package org.renjin.compiler.pipeline.fusion.kernel;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.fusion.node.LoopNode;
import org.renjin.compiler.pipeline.node.DeferredNode;

/**
 * Specializes a function for specific operands.
 *
 */
public interface LoopKernel {
  void compute(ComputeMethod method, DeferredNode node);
  
  String debugLabel(LoopNode[] operands);
}
