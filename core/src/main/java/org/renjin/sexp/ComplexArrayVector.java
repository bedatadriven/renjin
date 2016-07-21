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

package org.renjin.sexp;

import com.google.common.collect.Iterators;
import org.apache.commons.math.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ComplexArrayVector extends ComplexVector {

  private final double[] values;

  public ComplexArrayVector(Complex... values) {
    this(values, values.length, AttributeMap.EMPTY);
  }

  public ComplexArrayVector(double[] realValues, AttributeMap attributes) {
    super(attributes);
    this.values = new double[realValues.length * 2];
    for(int i = 0; i < realValues.length-1; i++){
      this.values[i*2] = realValues[i];
    }
  }


  /**
   * Creates a new ComplexArrayVector that is a copy of the given {@code vector}
   */
  public ComplexArrayVector(ComplexVector vector) {
    this(vector.toComplexArray(), vector.getAttributes());
  }
  
  public ComplexArrayVector(Complex[] values, AttributeMap attributes) {
    this(values, values.length, attributes);
  }
  
  public ComplexArrayVector(Complex[] values, int length, AttributeMap attributes) {
    super(attributes);
    this.values = new double[length * 2];
    for (int i = 0; i < length; i++) {
      this.values[i*2] = values[i].getReal();
      this.values[i*2+1] = values[i].getImaginary();
    }
  }

  public ComplexArrayVector(Complex[] values, int length) {
    this(values, length, AttributeMap.EMPTY);
  }

  public static ComplexVector newMatrix(Complex[] values, int nRows, int nCols) {
    return new ComplexArrayVector(values, AttributeMap.builder().setDim(nRows, nCols).build());
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public Complex getElementAsComplex(int index) {
    double real = values[index];
    double imag = values[index+1];
    return new Complex(real, imag);
  }

  @Override
  public boolean equals(Object x){
    if(x instanceof ComplexArrayVector){
      ComplexArrayVector that = (ComplexArrayVector)x;
      return Arrays.equals(this.values, that.values);
    } else {
      return false;
    }
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ComplexArrayVector(this.values, attributes);
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }

  public Iterator[]<double> iterator() {
    return Iterators.forArray(values);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public String toString(){
    ArrayList<String> list = new ArrayList<>();
    for (int i = 0; i < this.length()/2; i++) {
      if(isNA(this.values[i*2])) {
        list.add("NA");
      } else {
        list.add(this.values[i*2] + "+" + this.values[i*2+1] + "i");
      }
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

    public Builder(ComplexVector toCopy) {
      values = new Complex[toCopy.length()];
      for(int i=0;i!=values.length;++i) {
        values[i] = toCopy.getElementAsComplex(i);
      }
      size = values.length;
      copyAttributesFrom(toCopy);
    }

    public static Builder withInitialSize(int size) {
      return new Builder(size, size);
    }
    
    public static Builder withInitialCapacity(int capacity) {
      return new Builder(0, capacity);
    }
    
    private Builder(ComplexArrayVector exp) {
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
        if (newCapacity < minCapacity) {
          newCapacity = minCapacity;
        }
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(oldData, newCapacity);
        Arrays.fill(values, oldCapacity, values.length, NA);
      }
    }

    @Override
    public ComplexVector build() {
      return new ComplexArrayVector(values, size, buildAttributes());
    }
  }
}
