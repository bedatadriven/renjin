/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.sexp;

import org.apache.commons.math.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ComplexArrayVector extends ComplexVector {

  private final double[] values;

  public ComplexArrayVector(Complex... values) {
    this(values, values.length, AttributeMap.EMPTY);
  }

  /**
   *
   * @param realComplexValues this is an array that contains two double values for each complex number first value
   *                          being the real component and second value the imaginary.
   */
  private ComplexArrayVector(double[] realComplexValues, AttributeMap attributes) {
    super(attributes);
    this.values = realComplexValues;
  }


  /**
   * Creates a new ComplexArrayVector that is a copy of the given {@code vector}
   */
  public ComplexArrayVector(ComplexVector vector) {
    this(vector.toComplexArray(), vector.getAttributes());
  }

  public static ComplexArrayVector fromRealArray(double[] realValues, AttributeMap attributes) {
    double[] values = new double[realValues.length * 2];
    for(int i = 0; i < realValues.length; i++){
      values[i*2] = realValues[i];
    }
    return new ComplexArrayVector(values, attributes);
  }

  public double[] toComplexArrayVectorUnsafe() {
    return values;
  }
  
  public ComplexArrayVector(Complex[] values, AttributeMap attributes) {
    this(values, values.length, attributes);
  }
  
  public ComplexArrayVector(Complex[] values, int length, AttributeMap attributes) {
    super(attributes);
    this.values = new double[length * 2];
    for (int i = 0; i < length; ++i) {
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
    return this.values.length / 2;
  }

  @Override
  public Complex getElementAsComplex(int index) {
    double real = values[index*2];
    double imag = values[index*2+1];
    return new Complex(real, imag);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ComplexArrayVector(this.values, attributes);
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(new Complex(values[index*2], values[index*2+1]));
  }

  public Iterator<Complex> iterator() {
    return Arrays.asList(toComplexArray()).iterator();
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public String toString(){
    ArrayList<String> list = new ArrayList<>();
    for (int i = 0; i < this.length(); ++i) {
      if(isNA(new Complex(this.values[i*2], this.values[i*2+1]))) {
        list.add("NA");
      } else {
        list.add(this.values[i*2] + "+" + this.values[i*2+1] + "i");
      }
    }
    return list.toString();
  }
  
  public static class Builder extends AbstractAtomicBuilder{
    private static final int MIN_INITIAL_CAPACITY = 50;
    private double builderValues[];
    private int size;

    public Builder(int initialSize, int initialCapacity) {
      if(initialCapacity < MIN_INITIAL_CAPACITY) {
        initialCapacity = MIN_INITIAL_CAPACITY;
      }
      if(initialSize > initialCapacity) {
        initialCapacity = initialSize;
      }
      builderValues = new double[initialCapacity*2];
      size = initialSize;
      Arrays.fill(builderValues, DoubleVector.NA);
    }
    

    public Builder() {
      this(0, MIN_INITIAL_CAPACITY);
    }

    public Builder(int initialSize) {
      this(initialSize, initialSize);
    }

    public Builder(ComplexVector toCopy) {
      builderValues = new double[toCopy.length() * 2];
      for(int i =  0; i < toCopy.length(); ++i) {
        builderValues[i*2] = toCopy.getElementAsComplex(i).getReal();
        builderValues[i*2+1]  = toCopy.getElementAsComplex(i).getImaginary();
      }
      size = builderValues.length/2;
      copyAttributesFrom(toCopy);
    }

    public static Builder withInitialSize(int size) {
      return new Builder(size, size);
    }
    
    public static Builder withInitialCapacity(int capacity) {
      return new Builder(0, capacity);
    }
    
    private Builder(ComplexArrayVector exp) {
      this.builderValues = Arrays.copyOf(exp.values, exp.values.length);
      this.size = exp.values.length/2;

      copyAttributesFrom(exp);
    }


    public Builder set(int index, double real, double imaginary) {
      ensureCapacity(index+1);
      if(index+1 > size) {
        this.size = index+1;
      }
      this.builderValues[index*2] = real;
      this.builderValues[index*2+1] = imaginary;
      return this;
    }

    public Builder set(int index, Complex value) {
      return set(index, value.getReal(), value.getImaginary());
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

    @Override
    public int length() {
      return size;
    }

    public void ensureCapacity(int minCapacity) {
      int oldCapacity = builderValues.length/2;
      if (minCapacity > oldCapacity) {
        double oldData[] = builderValues;
        int newCapacity = (oldCapacity * 3)/2 + 1;
        if (newCapacity < minCapacity) {
          newCapacity = minCapacity;
        }
        // minCapacity is usually close to size, so this is a win:
        builderValues = Arrays.copyOf(oldData, newCapacity*2);
        Arrays.fill(builderValues, oldCapacity*2, builderValues.length, DoubleVector.NA);
      }
    }

    @Override
    public ComplexVector build() {
      if (this.builderValues.length != size*2) {
        double[] reseizedArray = Arrays.copyOf(builderValues, size*2);
        return new ComplexArrayVector(reseizedArray, buildAttributes());
      } else {
        return new ComplexArrayVector(this.builderValues, buildAttributes());
      }
    }
  }
}
