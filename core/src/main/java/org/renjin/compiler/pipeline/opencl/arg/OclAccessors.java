package org.renjin.compiler.pipeline.opencl.arg;

import org.renjin.compiler.pipeline.DeferredNode;


public class OclAccessors {

  public static OclAccessor get(DeferredNode node, ArgumentMap argumentMap) {
    if(OclArrayAccessor.accept(node)) {
      return new OclArrayAccessor(node, argumentMap);
    }
    throw new UnsupportedOperationException(node.toString());
  }
}
