package org.renjin.sexp;

import java.util.Iterator;

import org.apache.commons.math.complex.Complex;
import org.renjin.parser.ParseUtil;

import com.google.common.collect.UnmodifiableIterator;


public abstract class IntVector extends AbstractAtomicVector implements Iterable<Integer> {
  public static final String TYPE_NAME = "integer";
  public static final int TYPE_CODE = 13;
  public static final Type VECTOR_TYPE = new IntType();
  public static final IntVector EMPTY = new IntArrayVector();
  public static final String IMPLICIT_CLASS = "integer";

  /**
   * The integer constant used to designate elements or values that are
   * missing in the statistical sense, or literally "Not Available".
   */
  public static final int NA = Integer.MIN_VALUE;

  protected IntVector() {
    super();
  }

  protected IntVector(AttributeMap attributes) {
    super(attributes);
  }

  public static boolean isNA(int value) {
    return value == NA;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public abstract int length();

  @Override
  public abstract int getElementAsInt(int i);

  @Override
  public Logical asLogical() {
    if (length() == 0) {
      return Logical.NA;
    }
    return getElementAsLogical(0);
  }

  @Override
  public double getElementAsDouble(int index) {
    return isElementNA(index) ? DoubleVector.NA : getElementAsInt(index);
  }

  @Override
  public String getElementAsString(int index) {
    return isElementNA(index) ? StringVector.NA : ParseUtil.toString(getElementAsInt(index));
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new IntArrayVector(getElementAsInt(index));
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return new Complex(getElementAsDouble(index), 0);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    int value = getElementAsInt(index);
    if (value == 0 || isNA(value)) {
      return value;
    } else {
      return 1;
    }
  }

  @Override
  public Integer getElementAsObject(int index) {
    return getElementAsInt(index);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    int value = vector.getElementAsInt(vectorIndex);
    for (int i = startIndex; i < length(); ++i) {
      if (value == getElementAsInt(i)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  protected abstract SEXP cloneWithNewAttributes(AttributeMap attributes);

  @Override
  public int compare(int index1, int index2) {
    return getElementAsInt(index1) - getElementAsInt(index2);
  }

  @Override
  public IntArrayVector.Builder newBuilderWithInitialSize(int initialSize) {
    return new IntArrayVector.Builder(initialSize, initialSize);
  }

  @Override
  public IntArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new IntArrayVector.Builder(0, initialCapacity);
  }

  @Override
  public IntArrayVector.Builder newCopyBuilder() {
    return new IntArrayVector.Builder(this);
  }

  @Override
  public boolean isNumeric() {
    return !inherits("factor");
  }

  @Override
  public String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  @Override
  public double asReal() {
    if (length() == 0 || isElementNA(0)) {
      return DoubleVector.NA;
    } else {
      return getElementAsDouble(0);
    }
  }

  public int[] toIntArray() {
    int[] array = new int[length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = getElementAsInt(i);
    }
    return array;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new ValueIterator();
  }

  @Override
  public double[] toDoubleArray() {
    double[] d = new double[length()];
    for (int i = 0; i != d.length; ++i) {
      int x = getElementAsInt(i);
      if (x == NA) {
        d[i] = DoubleVector.NA;
      } else {
        d[i] = x;
      }
    }
    return d;
  }


  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof IntVector)) return false;

    IntVector that = (IntVector) o;
    if(that.length() != this.length()) {
      return false;
    }

    for(int i=0;i!=length();++i) {
      if(getElementAsInt(i)!=that.getElementAsInt(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final int hashCode() {
    int hash = 37;
    for(int i=0;i!=length();++i) {
      hash += getElementAsInt(i);
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("c(");
    for (int i = 0; i < Math.min(5, length()); ++i) {
      if (i > 0) {
        sb.append(", ");
      }
      if (isElementNA(i)) {
        sb.append("NA_integer_");
      } else {
        sb.append(getElementAsInt(i)).append("L");
      }
    }
    if (length() > 5) {
      sb.append("...").append(length()).append(" elements total");
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(getElementAsInt(index));
  }

  private static class IntType extends Type {
    private IntType() {
      super(Order.INTEGER);
    }

    @Override
    public IntArrayVector.Builder newBuilder() {
      return new IntArrayVector.Builder(0, 0);
    }

    @Override
    public IntArrayVector.Builder newBuilderWithInitialSize(int initialSize) {
      return new IntArrayVector.Builder(initialSize);
    }

    @Override
    public IntArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new IntArrayVector.Builder(0, initialCapacity);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new IntArrayVector(vector.getElementAsInt(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      assert !vector1.isElementNA(index1) && !vector2.isElementNA(index2);
      return vector1.getElementAsInt(index1) - vector2.getElementAsInt(index2);
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      if(vector1.isElementNA(index1) || vector2.isElementNA(index2)) {
        return false;
      }
      return vector1.getElementAsInt(index1) == vector2.getElementAsInt(index2);
    }
    
    
  }

  private class ValueIterator extends UnmodifiableIterator<Integer> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < length();
    }

    @Override
    public Integer next() {
      return getElementAsInt(i++);
    }
  }

  public static SEXP valueOf(int value) {
    return new IntArrayVector(value);
  }
}
