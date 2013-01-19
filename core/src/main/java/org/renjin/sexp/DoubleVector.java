package org.renjin.sexp;

import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.math.complex.Complex;
import org.renjin.parser.ParseUtil;

import java.util.Iterator;


public abstract class DoubleVector extends AbstractAtomicVector implements Iterable<Double> {

  public static final String TYPE_NAME = "double";

  public static final Type VECTOR_TYPE = new DoubleType();

  /**
   * This is the internal representation R uses to
   * represent NAs: a "quiet NaN" with a payload of 1954 (0x07A2).
   * <p/>
   * <p>The Java Language Spec is somewhat ambiguous regarding the extent to which
   * non-canonical NaNs will be preserved. What is clear though, is that signaled bit
   * (bit 12) is dropped by {@link Double#longBitsToDouble(long)}, at least on the few
   * platforms on which I have tested the Sun JDK 1.6.
   * <p/>
   * <p>The payload, however, does appear to be preserved by the JVM.
   */
  public static final long NA_BITS = 0x7FF00000000007A2L;

  protected static final long LOWER_WORD_MASK = 0x00000000FFFFFFFFL;

  
  /**
   * The double constant used to designate elements or values that are
   * missing in the statistical sense, or literally "Not Available". The following
   * has the relationships hold true:
   * <p/>
   * <ul>
   * <li>isNaN(NA) is <i>true</i>
   * <li>isNA(Double.NaN) is <i>false</i>
   * </ul>
   */
  public static final double NA = Double.longBitsToDouble(NA_BITS);

  public static final double NaN = Double.NaN;
  public static final double EPSILON = 2.220446e-16;

  public static final DoubleVector EMPTY = new DoubleArrayVector();
  public static final int NA_PAYLOAD = 1954;

  protected DoubleVector(AttributeMap attributes) {
    super(attributes);
  }

  protected DoubleVector() {

  }

  public static boolean isNaN(double x) {
    return Double.isNaN(x);
  }

  public static boolean isNA(double input) {
    if(Double.isNaN(input)) {
      long bits = Double.doubleToRawLongBits(input);
      long lowerWord = bits & LOWER_WORD_MASK;
      return lowerWord == NA_PAYLOAD;
    } else {
      return false;
    }
  }

  public static boolean isFinite(double d) {
    return !Double.isInfinite(d) && !Double.isNaN(d);
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
  protected abstract SEXP cloneWithNewAttributes(AttributeMap attributes);

  public double get(int i) {
    return getElementAsDouble(i);
  }

  @Override
  public Logical getElementAsLogical(int index) {
    double value = getElementAsDouble(index);
    if (isNA(value)) {
      return Logical.NA;
    } else if (value == 0) {
      return Logical.FALSE;
    } else {
      return Logical.TRUE;
    }
  }

  @Override
  public int getElementAsRawLogical(int index) {
    double value = getElementAsDouble(index);
    if (isNaN(value)) {
      return IntVector.NA;
    } else if (value == 0) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public String getElementAsString(int index) {
    double value = getElementAsDouble(index);
    if (isNA(value)) {
      return StringVector.NA;
    } else if (isNaN(value)) {
      return "NaN";
    } else {
      return ParseUtil.toString(value);
    }
  }

  @Override
  public int getElementAsInt(int index) {
    double value = getElementAsDouble(index);
    return Double.isNaN(value) ? IntVector.NA : (int) value;
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return new Complex(getElementAsDouble(index), 0);
  }

  @Override
  public abstract double getElementAsDouble(int index);

  public DoubleVector getElementAsSEXP(int index) {
    return new DoubleArrayVector(getElementAsDouble(index));
  }

  @Override
  public Double getElementAsObject(int index) {
    return getElementAsDouble(index);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    double value = vector.getElementAsDouble(vectorIndex);
    for (int i = startIndex; i < length(); ++i) {
      if (value == getElementAsDouble(i)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int compare(int index1, int index2) {
    return Double.compare(getElementAsDouble(index1), getElementAsDouble(index2));
  }

  @Override
  public abstract int length();

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    return getElementAsLogical(0);
  }

  @Override
  public String getImplicitClass() {
    return "numeric";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Double> iterator() {
    return new UnmodifiableIterator<Double>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index != length();
      }

      @Override
      public Double next() {
        return getElementAsDouble(index++);
      }
    };
  }

  public double asReal() {
    if (length() == 0) {
      return NA;
    } else {
      return getElementAsDouble(0);
    }
  }


  @Override
  public double[] toDoubleArray() {
    double[] array = new double[length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = getElementAsDouble(i);
    }
    return array;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof DoubleVector)) return false;

    DoubleVector vector = (DoubleVector) o;

    if (this.length() != vector.length()) {
      return false;
    }
    for (int i = 0; i != length(); ++i) {
      double this_i = getElementAsDouble(i);
      double that_i = vector.getElementAsDouble(i);

      if (isNA(this_i) != isNA(that_i)) {
        return false;
      }
      if (isNaN(this_i) != isNaN(that_i)) {
        return false;
      }
      if (!isNaN(this_i) && !isNaN(that_i) && this_i != that_i) {
        return false;
      }
    }

    return true;
  }

  @Override
  public final int hashCode() {
    int hash = 37;
    for(int i=0;i!=length();++i) {
      long value = Double.doubleToRawLongBits(getElementAsDouble(i));
      hash += (int)( value ^ (value >>> 32));
    }
    return hash;
  }

  @Override
  public DoubleArrayVector.Builder newCopyBuilder() {
    return new DoubleArrayVector.Builder(this);
  }

  @Override
  public DoubleArrayVector.Builder newBuilderWithInitialSize(int initialSize) {
    return new DoubleArrayVector.Builder(initialSize);
  }

  @Override
  public DoubleArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new DoubleArrayVector.Builder(0, initialCapacity);
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(getElementAsDouble(index));
  }

  public boolean isElementNaN(int index) {
    return isNaN(getElementAsDouble(index));
  }

  private static class DoubleType extends Type {
    public DoubleType() {
      super(Order.DOUBLE);
    }

    @Override
    public DoubleArrayVector.Builder newBuilder() {
      return new DoubleArrayVector.Builder(0, 0);
    }

    @Override
    public DoubleArrayVector.Builder newBuilderWithInitialSize(int length) {
      return new DoubleArrayVector.Builder(length);
    }

    @Override
    public DoubleArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new DoubleArrayVector.Builder(0, initialCapacity);
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return Double.compare(vector1.getElementAsDouble(index1), vector2.getElementAsDouble(index2));
    }
    
    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      return vector1.getElementAsDouble(index1) == vector2.getElementAsDouble(index2);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new DoubleArrayVector(vector.getElementAsDouble(index));
    }
  }
}
