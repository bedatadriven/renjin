package org.renjin.primitives.sequence;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class RepLogicalVector extends LogicalVector {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepLogicalVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }

  public RepLogicalVector(Vector source, int length, int each) {
    this(source, length, each, transformAttributes(source, length, each));
  }

  private static AttributeMap transformAttributes(Vector source, int length, int each) {
    if(source.getAttributes().hasNames()) {
      return source.getAttributes()
              .copy()
              .setNames(new RepStringVector(source.getAttributes().getNames(), length, each, AttributeMap.EMPTY))
              .build();
    } else {
      return source.getAttributes();
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepLogicalVector(source, length, each, attributes);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return source.getElementAsInt((index / each) % source.length());
  }
  
  public static Builder newConstantBuilder(Logical value, int length) {
    return new Builder(value, length);
  }
  
  public static class Builder extends AbstractAtomicBuilder {
      private LogicalVector vector;
      private int length = 1;
      private int each = 1;

      public Builder(Logical value, int length) {
        switch (value) {
        case TRUE:
          this.vector = LogicalVector.TRUE;
          break;
        case FALSE:
          this.vector = LogicalVector.FALSE;
          break;
        default:
          this.vector = LogicalVector.NA_VECTOR;
          break;
        }
        this.length = length;
      }
      
      @Override
      public int length() {
        return length;
      }

      @Override
      public LogicalVector build() {
        return new RepLogicalVector(vector, length, each, buildAttributes());
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
