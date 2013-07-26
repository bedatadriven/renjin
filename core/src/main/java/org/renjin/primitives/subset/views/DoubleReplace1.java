package org.renjin.primitives.subset.views;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class DoubleReplace1 extends DoubleVector implements DeferredComputation {
  
  private Vector source;
  private Vector replacement;
  private Vector index;
  
  private int indexToReplace;
  private double replacementValue;

  public DoubleReplace1(Vector source, Vector replacement, Vector index, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.replacement = replacement;
    this.index = index;
    
    if(index.length() != 1) {
      throw new IllegalArgumentException("index must be length 1");
    }
   
    indexToReplace = index.getElementAsInt(0);
    replacementValue = index.getElementAsDouble(0);
    
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, replacement, index };
  }

  @Override
  public String getComputationName() {
    return "replace1";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleReplace1(source, replacement, index, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(index == indexToReplace) {
      return replacementValue;
    } else {
      return source.getElementAsDouble(index) ;
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return source.length();
  }
}
