package org.renjin.primitives.vector;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class ConstantLogicalVector extends LogicalVector {

  private int value;
  private int length;

  public ConstantLogicalVector(int value, int length, AttributeMap attributes) {
    super(attributes);
    this.value = value;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ConstantLogicalVector(value, length, attributes);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return value;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }
  
  public static Builder newConstantBuilder(int value, int length) {
    return new Builder(value, length);
  }
  
  public static class Builder extends AbstractAtomicBuilder {
	    private int value;
	    private int size;

	    public Builder(int value, int length) {
	      this.value = value;
	      this.size = length;
	    }
	    
	    @Override
	    public int length() {
	      return size;
	    }

	    @Override
	    public LogicalVector build() {
	      return new ConstantLogicalVector(value, size, buildAttributes());
	    }

      @Override
      public Builder setNA(int index) {
        throw new EvalException("cannot set na on constant builder");
      }

      @Override
      public Builder setFrom(int destinationIndex,
          Vector source, int sourceIndex) {
        throw new EvalException("cannot set from on constant builder");
      }

      @Override
      public Builder add(Number value) {
       throw new EvalException("cannot add something to constant builder");
      }
	  }
}
