package org.renjin.sexp;

import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.math.complex.Complex;

import java.util.Iterator;


public abstract class LogicalVector extends AbstractAtomicVector implements Iterable<Logical> {
  public static final String TYPE_NAME = "logical";
  public static final Type VECTOR_TYPE = new LogicalType();
  public static final LogicalVector EMPTY = new LogicalArrayVector(new int[0]);
  public static int NA = IntVector.NA;
  public static LogicalVector TRUE = new LogicalArrayVector(1);
  public static LogicalVector FALSE = new LogicalArrayVector(0);
  public static LogicalVector NA_VECTOR = new LogicalArrayVector(NA);



  public static SEXP valueOf(boolean value) {
    return value ? TRUE : FALSE;
  }
  
  public LogicalVector(AttributeMap attributes) {
    super(attributes);
  }

  protected LogicalVector() {
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public abstract int length();

  @Override
  public int getElementAsInt(int index) {
    return getElementAsRawLogical(index);
  }

  @Override
  public double getElementAsDouble(int index) {
    int value = getElementAsRawLogical(index);
    return value == IntVector.NA ? DoubleVector.NA : (double) value;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new LogicalArrayVector(getElementAsRawLogical(index));
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    int value = vector.getElementAsRawLogical(startIndex);
    for(int i=0;i<length();++i) {
      if(value ==  getElementAsRawLogical(i)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int compare(int index1, int index2) {
    return getElementAsRawLogical(index1) - getElementAsRawLogical(index2);
  }

  @Override
  public Boolean getElementAsObject(int index) {
    int value = getElementAsInt(index);
    if(IntVector.isNA(value)) {
      throw new IllegalStateException(String.format("The element at index %d is NA," +
          " and cannot be represented as a Boolean. Make sure you are calling isElementNA() first.", index));
    } else {
      return value != 0;
    }
  }

  @Override
  public Logical getElementAsLogical(int index) {
    return Logical.valueOf(getElementAsRawLogical(index));
  }

  @Override
  public abstract int getElementAsRawLogical(int index);

  @Override
  public Complex getElementAsComplex(int index) {
    if(IntVector.isNA(getElementAsRawLogical(index))) {
      return ComplexVector.NA;
    }
    return new Complex(getElementAsDouble(index), 0);
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public String getElementAsString(int index) {
    int value = getElementAsRawLogical(index);
    if(value == IntVector.NA) {
      return StringVector.NA;
    } else if(value == 0) {
      return "FALSE";
    } else {
      return "TRUE";
    }
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    return getElementAsLogical(0);
  }

  @Override
  public double asReal() {
    if(length() == 0) {
      return DoubleVector.NA;
    } else {
      return getElementAsDouble(0);
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Logical> iterator() {
    return new UnmodifiableIterator<Logical>() {
      private int i=0;

      @Override
      public boolean hasNext() {
        return i<length();
      }

      @Override
      public Logical next() {
        return getElementAsLogical(i++);
      }
    };
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof LogicalVector)) return false;

    LogicalVector that = (LogicalVector) o;
    if(this.length() != that.length()) {
      return false;
    }
    for(int i=0;i!=length();++i) {
      if(this.getElementAsRawLogical(i) != that.getElementAsRawLogical(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final int hashCode() {
    int hash = 37;
    for(int i=0;i!=length();++i) {
      hash += getElementAsRawLogical(i);
    }
    return hash;
  }

  @Override
  public String toString() {
    if (length() == 1) {
      return toString(getElementAsRawLogical(0));
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("c(");
      for (int i = 0; i != Math.min(5, length()); ++i) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(toString(getElementAsRawLogical(i)));
      }
      if (length() > 5) {
        sb.append(",... ").append(length()).append(" elements total");
      }
      sb.append(")");
      return sb.toString();
    }
  }

  @Override
  public LogicalArrayVector.Builder newCopyBuilder() {
    return new LogicalArrayVector.Builder(this);
  }

  @Override
  public LogicalArrayVector.Builder newBuilderWithInitialSize(int initialSize) {
    return new LogicalArrayVector.Builder(initialSize, initialSize);
  }

  @Override
  public LogicalArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new LogicalArrayVector.Builder(0, initialCapacity);
  }

  @Override
  protected abstract SEXP cloneWithNewAttributes(AttributeMap attributes);

  private String toString(int x) {
    if (x == 1) {
      return "TRUE";
    } else if (x == 0) {
      return "FALSE";
    } else {
      return "NA";
    }
  }

  @Override
  public boolean isElementNA(int index) {
    return IntVector.isNA(getElementAsRawLogical(index));
  }


  private static class LogicalType extends Vector.Type {
    public LogicalType() {
      super(Order.LOGICAL);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new LogicalArrayVector.Builder(0, 0);
    }

    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new LogicalArrayVector.Builder(initialSize);
    }

    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new LogicalArrayVector.Builder(0, initialCapacity);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new LogicalArrayVector(vector.getElementAsRawLogical(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsRawLogical(index1) - vector2.getElementAsRawLogical(index2);
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      if(vector1.isElementNA(index1) || vector2.isElementNA(index2)) {
        return false;
      }
      return vector1.getElementAsRawLogical(index1) == vector2.getElementAsRawLogical(index2);
    }
  }

}
