package org.renjin.primitives.subset;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

/**
 * Created by alex on 22-3-16.
 */
public class MaskedDoubleReplacement extends DoubleVector implements MemoizedComputation {

  private AtomicVector source;
  private LogicalVector mask;
  private AtomicVector replacement;

  private DoubleVector result = null;
  
  public MaskedDoubleReplacement(AttributeMap attributes, AtomicVector source, LogicalVector mask, AtomicVector replacement) {
    super(attributes);
    this.source = source;
    this.mask = mask;
    this.replacement = replacement;
  }


  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, mask, replacement };
  }

  @Override
  public String getComputationName() {
    return "[[<-";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new MaskedDoubleReplacement(attributes, source, mask, replacement);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(result == null) {
      throw new IllegalStateException("Not computed");
    }
    return result.getElementAsDouble(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return source.length();
  }

  @Override
  public Vector forceResult() {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder(source.length(), source.length());
    int maskLength = mask.length();
    int replacementLength = replacement.length();
    int replacementIndex = 0;
    for (int i = 0; i < source.length(); i++) {
      int maskValue = mask.getElementAsRawLogical(i % maskLength);
      if(maskValue == 1) {
        result.set(i, replacement.getElementAsDouble((replacementIndex++) % replacementLength)); 
      } else {
        result.set(i, source.getElementAsDouble(i));
      }
    }
    this.result = result.build();
    return this.result;
  }

  @Override
  public void setResult(Vector result) {
    this.result = (DoubleVector) result;
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }

}
