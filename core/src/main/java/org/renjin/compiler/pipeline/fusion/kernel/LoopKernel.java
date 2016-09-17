package org.renjin.compiler.pipeline.fusion.kernel;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.fusion.node.LoopNode;

/**
 * Specializes a function for specific operands.
 *
 */
public interface LoopKernel {
  void compute(ComputeMethod method, LoopNode node[]);
  
  String debugLabel(LoopNode[] operands);
}
