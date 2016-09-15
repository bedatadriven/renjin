package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.node.DeferredNode;

/**
 * Specializes a function for specific operands.
 *
 */
public interface FunctionSpecializer {
  void compute(ComputeMethod method, DeferredNode node);
}
