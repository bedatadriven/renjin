package org.renjin.compiler.pipeline.opencl.arg;


import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;

public class OclArrayAccessor extends OclAccessor {

  private final String bufferName;
  private final String lengthName;

  public static boolean accept(DeferredNode node) {
    return node.getVector() instanceof DoubleArrayVector ||
        node.getVector() instanceof IntArrayVector;
  }

  public OclArrayAccessor(DeferredNode node, ArgumentMap map) {
    this.bufferName = map.getValueExpr(node);
    this.lengthName = map.getLengthExpr(node);
  }


  @Override
  public String value() {
    return bufferName;
  }

  @Override
  public String length() {
    return lengthName;
  }

  @Override
  public String valueAt(String index) {
    return bufferName + "[" + index + "]";
  }
}
