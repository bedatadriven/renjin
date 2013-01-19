package org.renjin.sexp;

import com.google.common.base.Joiner;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Arrays;
import java.util.Iterator;

public class RawVector extends AbstractAtomicVector implements Iterable<Raw> {

  public static final String TYPE_NAME = "raw";
  public static final Vector.Type VECTOR_TYPE = new RawType();
  public static int NA = IntVector.NA;
  private Raw[] values;

  public RawVector(Raw... values) {
    this.values = new Raw[values.length];
    this.values = Arrays.copyOf(values, values.length);
  }
  
  
  public RawVector(Raw[] values, AttributeMap attributes) {
    super(attributes);
    this.values = new Raw[values.length];
    this.values = Arrays.copyOf(values, values.length);
  }
  
  public RawVector(byte[] bytes, AttributeMap attributes) {
    super(attributes);
    this.values = new Raw[bytes.length];
    for(int i=0;i!=bytes.length;++i) {
      this.values[i] = new Raw(bytes[i]);
    }
  }

  public RawVector(byte[] bytes) {
    this(bytes, AttributeMap.EMPTY);
  }

  public byte[] getAsByteArray(){
    byte[] bytes = new byte[this.values.length];
    for (int i=0;i<this.values.length;i++){
      bytes[i] = this.values[i].getAsByte();
    }
    return(bytes);
  }
  
  public Raw[] getAsRawArray() {
    Raw[] raws = new Raw[this.values.length];
    for (int i = 0; i < raws.length; i++) {
      raws[i] = new Raw(this.values[i].getValue());
    }
    return (raws);
  }

  @Override
  public String getTypeName() {
    return ("raw");
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }
  
  public Raw getElement(int index){
    return(this.values[index]);
  }

  @Override
  public double getElementAsDouble(int index) {
    return ((double) this.values[index].getValue());
  }

  @Override
  public int getElementAsInt(int index) {
    return (this.values[index].getValue());
  }

  @Override
  public String getElementAsString(int index) {
    return (this.values[index].toString());
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return (Logical.valueOf(this.values[index].getValue()).getInternalValue());
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
    return Arrays.hashCode(this.getAsByteArray());
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
    return (values[index].getValue() == RawVector.NA);
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
    return (values[index1].getValue() - values[index2].getValue());
  }

  @Override
  public Iterator<Raw> iterator() {
    return new ValueIterator();
  }

  /*
   * Builder private class
   */
  public static class Builder extends AbstractAtomicBuilder {

    private Raw[] values;

    public Builder(int initialSize) {
      values = new Raw[initialSize];
      for (int i = 0; i < initialSize; i++) {
        values[i] = new Raw();
      }
    }

    private Builder(RawVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      copyAttributesFrom(exp);
    }

    public Builder() {
      this.values = new Raw[0];
    }

    /*
     * ArrayList does the same work, we may change the code
     * as well as using the Java core library
     */
    public Builder set(int index, Raw raw) {
      if (values.length <= index) {
        Raw copy[] = Arrays.copyOf(values, index + 1);
        Arrays.fill(copy, values.length, copy.length, new Raw(0));
        values = copy;
      }
      values[index].setValue(raw.getValue());
      return this;
    }

    public Builder add(Raw value) {
      return set(values.length, value);
    }

    public Builder add(Number value) {
      return add(new Raw(value.intValue()));
    }
    
    @Override
    public Builder setNA(int index) {
      return set(index, new Raw(RawVector.NA));
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, new Raw(source.getElementAsInt(sourceIndex)));
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
   * RawType private class
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
      return (new RawVector(new Raw(vector.getElementAsInt(index))));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsInt(index1) - vector2.getElementAsInt(index2);
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      // raws cannot be NA
      return vector1.getElementAsInt(index1) == vector2.getElementAsInt(index2);
    }
  }  

  @Override
  public String toString() {
    if (values.length == 1) {
      return values[0].toString();
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("c(");
      Joiner.on(", ").appendTo(sb, this);
      return sb.append(")").toString();
    }
  }

  private class ValueIterator extends UnmodifiableIterator<Raw> {

    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < values.length;
    }

    @Override
    public Raw next() {
      return values[i++];
    }
  }
}
