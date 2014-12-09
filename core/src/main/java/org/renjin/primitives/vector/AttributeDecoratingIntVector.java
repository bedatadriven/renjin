package org.renjin.primitives.vector;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Decorates an existing vector with new attributes.
 *
 */
public class AttributeDecoratingIntVector extends IntVector implements
    DeferredComputation {

  private final Vector vector;

  public AttributeDecoratingIntVector(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingIntVector(vector, attributes);
  }

  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }

  @Override
  public int length() {
    return vector.length();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "attr";
  }

  @Override
  public int getElementAsInt(int i) {
    return vector.getElementAsInt(i);
  }
  

  public static Builder newAttributeDecoratingIntBuilder(IntVector vector) {
    return new Builder(vector);
  }
  
  public static class Builder extends AbstractAtomicBuilder {
      private IntVector vector;

      public Builder(IntVector vector) {
        this.vector = vector;
      }
      
      @Override
      public int length() {
        return vector.length();
      }

      @Override
      public IntVector build() {
        return new AttributeDecoratingIntVector(vector, buildAttributes());
      }

      @Override
      public Builder setNA(int index) {
        throw new EvalException("cannot set na on decorating builder");
      }

      @Override
      public Builder setFrom(int destinationIndex,
          Vector source, int sourceIndex) {
        throw new EvalException("cannot set from on decorating builder");
      }

      @Override
      public Builder add(Number value) {
       throw new EvalException("cannot add something to decorating builder");
      }
    }
}
