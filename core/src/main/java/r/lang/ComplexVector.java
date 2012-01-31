/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.complex.Complex;

import com.google.common.collect.Iterators;
import static r.util.RInternalDSL.*;

public class ComplexVector extends AbstractAtomicVector implements Iterable<Complex> {

  private final Complex[] values;
  public static final String TYPE_NAME = "complex";
  public static final ComplexVector EMPTY = new ComplexVector();

  public static Vector.Type VECTOR_TYPE = new ComplexType();

  public static final Complex NA = new Complex(DoubleVector.NA, 0);

  public ComplexVector(Complex... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public ComplexVector(double[] values, PairList attributes) {
    super(attributes);
    this.values=new Complex[values.length];
    for(int i=0; i<values.length; i++){
      this.values[i]=complex(values[i]);
    }
  }
  
  public ComplexVector(Complex[] values, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, values.length);
  }
  
  public ComplexVector(Complex[] values, int length, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, length);
  }

  public static ComplexVector newMatrix(double[] values, int nRows, int nCols) {
    PairList attributes = new PairList.Node(Symbols.DIM, new IntVector(nRows,nCols), Null.INSTANCE);
    return new ComplexVector(values, attributes);
  }
  
  public static ComplexVector newMatrix(Complex[] values, int nRows, int nCols) {
    PairList attributes = new PairList.Node(Symbols.DIM,new IntVector(nRows,nCols), Null.INSTANCE);
    return new ComplexVector(values,attributes);
  }
  
  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public static boolean isNA(Complex value) {
    return DoubleVector.isNA(value.getReal());
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new ComplexVector(values[index]);
  }

  @Override
  public Comparable getElementAsObject(int index) {
    throw new UnsupportedOperationException("how can Complex not implement Comparable!!!");
  }

  @Override
  public double getElementAsDouble(int index) {
    return values[index].getReal();
  }

  @Override
  public int getElementAsInt(int index) {
    double value = values[index].getReal();
    return DoubleVector.isNA(value) ? IntVector.NA : (int)value;
  }

  @Override
  public String getElementAsString(int index) {
    Complex z = values[index];
    return z.getReal()+"+"+z.getImaginary()+"i";
  }

  @Override
  public int getElementAsRawLogical(int index) {
    double value = values[index].getReal();
    if(value == 0) {
      return 0;
    } else if(DoubleVector.isNA(value)) {
      return IntVector.NA;
    } else {
      return 1;
    }
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return values[index];
  }
  
  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    Complex value = vector.getElementAsComplex(vectorIndex);
    for(int i=startIndex;i<values.length;++i) {
      if(values[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public boolean equals(Object x){
    if(x instanceof ComplexVector){
      ComplexVector that = (ComplexVector)x;
      if(this.length()!=that.length()) return false;
      else{
        for(int i=0; i<this.length(); i++){
          if(!this.values[i].equals(that.values[i])){
            return false;
          }
        }
        return true;
      }
    }else return false;
  }
  
  @Override
  public int compare(int index1, int index2) {
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder(initialSize);
  }
  
  @Override
  public Builder newBuilderWithInitialCapacity(int intialCapacity) {
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public Builder newCopyBuilder() {
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public Iterator<Complex> iterator() {
    return Iterators.forArray(values);
  }

  @Override
  public boolean isElementNA(int index) {
    return values[index]==ComplexVector.NA;
  }
  
  @Override
  public String toString(){
    ArrayList<String> list = new ArrayList<String>();
    for(Complex z : values){
      list.add(z.getReal()+"+"+z.getImaginary()+"i");
    }
    return list.toString();
  }
  
  public static class Builder extends AbstractAtomicBuilder{
    private static final int MIN_INITIAL_CAPACITY = 50;
    private Complex values[];
    private int size;

    public Builder(int initialSize, int initialCapacity) {
      if(initialCapacity < MIN_INITIAL_CAPACITY) {
        initialCapacity = MIN_INITIAL_CAPACITY;
      }
      if(initialSize > initialCapacity) {
        initialCapacity = initialSize;
      }
      values = new Complex[initialCapacity];
      size = initialSize;
      Arrays.fill(values, NA);
    }
    

    public Builder() {
      this(0, MIN_INITIAL_CAPACITY);
    }

    public Builder(int initialSize) {
      this(initialSize, initialSize);
    }
    
    public static Builder withInitialSize(int size) {
      return new Builder(size, size);
    }
    
    public static Builder withInitialCapacity(int capacity) {
      return new Builder(0, capacity);
    }
    
    private Builder(ComplexVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      this.size = this.values.length;

      copyAttributesFrom(exp);
    }

    public Builder set(int index, Complex value) {
      ensureCapacity(index+1);
      if(index+1 > size) {
        size = index+1;
      }
      values[index] = value;
      return this;
    }

    public Builder add(Complex value) {
      return set(size, value);
    }

    @Override
    public Builder add(Number value) {
      return add(new Complex(value.doubleValue(),0));
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsComplex(sourceIndex));
    }

   
//    public Builder set(int index, Double value) {
//      return set(index, (double)value);
//    }

    @Override
    public int length() {
      return size;
    }

    public void ensureCapacity(int minCapacity) {
      int oldCapacity = values.length;
      if (minCapacity > oldCapacity) {
        Complex oldData[] = values;
        int newCapacity = (oldCapacity * 3)/2 + 1;
        if (newCapacity < minCapacity)
          newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(oldData, newCapacity);
        Arrays.fill(values, oldCapacity, values.length, NA);
      }
    }

    @Override
    public ComplexVector build() {
      return new ComplexVector(values, size, buildAttributes());
    }
  }

  private static class ComplexType extends Vector.Type {
    public ComplexType() {
      super(Order.COMPLEX);
    }

    @Override
    public Builder newBuilder() {
      return new Builder(0, 0);
    }

    @Override
    public Builder newBuilderWithInitialSize(int length) {
      return new Builder(length);
    }

    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new Builder(0, initialCapacity);
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      throw new UnsupportedOperationException("invalid comparison with complex values");
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new ComplexVector(vector.getElementAsComplex(index));
    }
  }

  
  

}
