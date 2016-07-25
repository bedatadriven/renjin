package org.renjin.primitives.combine.view;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

import java.util.List;

public class CombinedStringVector extends StringVector implements DeferredComputation {

  private final Vector[] vectors;
  private final int endIndex[];
  private final int totalLength;


  public static StringVector combine(Vector[] vectors, AttributeMap attributeMap) {
    if(vectors.length == 1) {
      return (StringVector) vectors[0].setAttributes(attributeMap);
    } else {
      return new CombinedStringVector(vectors, attributeMap);
    }
  }

  public static StringVector combine(List<Vector> vectors, AttributeMap attributeMap) {
    if(vectors.size() == 1) {
      return (StringVector) vectors.get(0).setAttributes(attributeMap);
    } else {
      return new CombinedStringVector(vectors.toArray(new Vector[vectors.size()]), attributeMap);
    }
  }

  private CombinedStringVector(Vector[] vectors, AttributeMap attributeMap) {
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
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new CombinedStringVector(vectors, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    if(index < endIndex[0]) {
      return vectors[0].getElementAsString(index);
    }
    if(index < endIndex[1]) {
      return vectors[1].getElementAsString(index - endIndex[0]);
    }
    if(index < endIndex[2]) {
      return vectors[2].getElementAsString(index - endIndex[1]);
    }
    if(index < endIndex[3]) {
      return vectors[3].getElementAsString(index - endIndex[2]);
    }
    for(int i=4; i < vectors.length;++i) {
      if(index < endIndex[i]) {
        return vectors[i].getElementAsString(index - endIndex[i-1]);
      }
    }
    throw new IllegalArgumentException("index: " + index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public int length() {
    return totalLength;
  }


}
