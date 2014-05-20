package org.renjin.sexp;

import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.UnsignedBytes;

import java.util.Arrays;
import java.util.Iterator;

public class RawVector extends AbstractAtomicVector implements Iterable<Byte> {

  public static final String TYPE_NAME = "raw";

  public static final Vector.Type VECTOR_TYPE = new RawType();

  public static final RawVector EMPTY = new RawVector();

  public static int NUM_BITS = 8;

  private byte[] values;

  public RawVector(byte... values) {
    this.values = new byte[values.length];
    this.values = Arrays.copyOf(values, values.length);
  }

  public RawVector(byte[] values, AttributeMap attributes) {
    super(attributes);
    this.values = new byte[values.length];
    this.values = Arrays.copyOf(values, values.length);
  }

  public byte[] toByteArray() {
    byte[] bytes = new byte[this.values.length];
    System.arraycopy(this.values, 0, bytes, 0, this.values.length);
    return(bytes);
  }

  @Override
  public String getTypeName() {
    return ("raw");
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }
  
  public byte getElementAsByte(int index){
    return(this.values[index]);
  }

  @Override
  public double getElementAsDouble(int index) {
    return (double)getElementAsInt(index);
  }

  @Override
  public int getElementAsInt(int index) {
    return UnsignedBytes.toInt(this.values[index]);
  }

  @Override
  public String getElementAsString(int index) {
    return toString(this.values[index]);
  }

  public static String toString(byte value) {
    if(value <= 0xF) {
      return "0" + Integer.toHexString(value);
    } else {
      return Integer.toHexString(value);
    }
  }

  public static byte cast(int value) {
    if(value < 0 || value > 255)  {
      return 0;
    } else {
      return (byte)value;
    }
  }


  @Override
  public int getElementAsRawLogical(int index) {
    return this.values[index] == 0 ? 0 : 1;
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RawVector(this.values, attributes);
  }


  @Override
  public SEXP getElementAsSEXP(int index) {
    return new RawVector(this.values[index]);
  }

  @Override
  public int length() {
    return this.values.length;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RawVector)) return (false);
    RawVector rv = (RawVector)o;
    return (rv.hashCode() == this.hashCode());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.toByteArray());
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return (new RawVector.Builder(initialSize));
  }
  
  

  @Override
  public Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new RawVector.Builder(0);
  }


  @Override
  public Type getVectorType() {
    return (VECTOR_TYPE);
  }

  @Override
  public Builder newCopyBuilder() {
    return (new RawVector.Builder(this));
  }

  @Override
  public boolean isElementNA(int index) {
    // Raws have no NA value
    return false;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public Object getElementAsObject(int index) {
    return (values[index]);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int compare(int index1, int index2) {
    return UnsignedBytes.compare(values[index1], values[index2]);
  }

  @Override
  public Iterator<Byte> iterator() {
    return new ValueIterator();
  }

  /*
   * Builder private class
   */
  public static class Builder extends AbstractAtomicBuilder {

    private byte[] values;

    public Builder(int initialSize) {
      values = new byte[initialSize];
    }

    private Builder(RawVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      copyAttributesFrom(exp);
    }

    public Builder() {
      this.values = new byte[0];
    }

    /*
     * ArrayList does the same work, we may change the code
     * as well as using the Java core library
     */
    public Builder set(int index, byte value) {
      if (values.length <= index) {
        values = Arrays.copyOf(values, index + 1);
      }
      values[index] = value;
      return this;
    }

    public Builder add(byte value) {
      return set(values.length, value);
    }

    public Builder add(Number value) {
      return add(UnsignedBytes.checkedCast(value.longValue()));
    }
    
    @Override
    public Builder setNA(int index) {
      return set(index, (byte)0);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, cast(source.getElementAsInt(sourceIndex)));
    }

    @Override
    public int length() {
      return values.length;
    }

    @Override
    public RawVector build() {
      return new RawVector(values, buildAttributes());
    }
  }

  /*
   * ByteType private class
   */
  private static class RawType extends Vector.Type {

    private RawType() {
      super(Order.RAW);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new RawVector.Builder(0);
    }
    
    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new RawVector.Builder(initialSize);
    }
    
    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new RawVector.Builder(0);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new RawVector(vector.getElementAsByte(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return UnsignedBytes.compare(vector1.getElementAsByte(index1), vector2.getElementAsByte(index2));
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      // raws cannot be NA
      return vector1.getElementAsByte(index1) == vector2.getElementAsByte(index2);
    }
  }  

  @Override
  public String toString() {
    if (values.length == 1) {
      return getElementAsString(0);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("c(");
      for(int i=0;i!=length();++i) {
        if(i>0) {
          sb.append(", ");
        }
        sb.append(getElementAsString(i));
      }
      return sb.append(")").toString();
    }
  }

  private class ValueIterator extends UnmodifiableIterator<Byte> {

    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < values.length;
    }

    @Override
    public Byte next() {
      return values[i++];
    }
  }
}
