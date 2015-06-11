package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;

/**
 * Specializes a function for specific operands.
 *
 */
public interface FunctionSpecializer {
  void compute(ComputeMethod method, DeferredNode node);
}
