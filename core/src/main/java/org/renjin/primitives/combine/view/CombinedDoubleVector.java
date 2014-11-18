package org.renjin.primitives.combine.view;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class CombinedDoubleVector extends DoubleVector implements DeferredComputation {

  private final Vector[] vectors;
  private final int endIndex[];
  private final int totalLength;

  public static DoubleVector combine(Vector[] vectors, AttributeMap attributeMap) {
    if(vectors.length == 1) {
      return (DoubleVector) vectors[0].setAttributes(attributeMap);
    } else {
      return new CombinedDoubleVector(vectors, attributeMap);
    }
  }

  private CombinedDoubleVector(Vector[] vectors, AttributeMap attributeMap) {
    super(attributeMap);

    this.vectors = vectors;
    this.endIndex = new int[vectors.length];

    int totalLength = 0;
    for(int i=0;i!=vectors.length;++i) {
      totalLength += vectors[i].length();
      endIndex[i] = totalLength;
    }
    this.totalLength = totalLength;
  }


  @Override
  public Vector[] getOperands() {
    return vectors;
  }

  @Override
  public String getComputationName() {
    return "c";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new CombinedDoubleVector(vectors, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(index < endIndex[0]) return vectors[0].getElementAsDouble(index);
    if(index < endIndex[1]) return vectors[1].getElementAsDouble(index - endIndex[0]);
    if(index < endIndex[2]) return vectors[2].getElementAsDouble(index - endIndex[1]);
    if(index < endIndex[3]) return vectors[3].getElementAsDouble(index - endIndex[2]);
    for(int i=4; i < vectors.length;++i) {
      if(index < endIndex[i]) {
        return vectors[i].getElementAsDouble(index - endIndex[i-1]);
      }
    }
    throw new IllegalArgumentException("index: " + index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return totalLength;
  }
}
