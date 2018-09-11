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
import org.renjin.eval.Profiler;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.Iterator;

public abstract class AbstractAtomicVector extends AbstractVector implements AtomicVector{

  protected AbstractAtomicVector(AttributeMap attributes) {
    super(attributes);
  }

  protected AbstractAtomicVector() {
  }

  @Override
  public boolean containsNA() {
    return indexOfNA() != -1;
  }


  @Override
  public boolean contains(Vector vector, int vectorIndex) {
    if(vector instanceof AtomicVector) {
      return contains((AtomicVector)vector, vectorIndex);
    } else {
      return false;
    }
  }
  
  @Override
  public Complex getElementAsComplex(int index) {
    return new Complex(getElementAsDouble(index), 0);
  }
  
  @Override
  public int indexOfNA() {
    for(int i=0;i!=length();++i) {
      if(isElementNA(i)) {
        return i;
      }
    }
    return -1;        
  }

  @Override
  public boolean contains(AtomicVector vector, int vectorIndex) {
    return indexOf(vector, vectorIndex, 0) != -1;
  }
  
  @Override
  public int indexOf(Vector vector, int vectorIndex, int startIndex) {
    if(vector instanceof AtomicVector) {
      return indexOf((AtomicVector)vector, vectorIndex, startIndex);
    } else {
      SEXP element = vector.getElementAsSEXP(vectorIndex);
      if(element instanceof AtomicVector && element.length() == 1) {
        return indexOf((AtomicVector)element, 0, startIndex);
      } else {
        return -1;
      }
    }
  }

  public final double asReal() {
    if (length() == 0) {
      return DoubleVector.NA;
    } else {
      return getElementAsDouble(0);
    }
  }

  public final int asInt() {
    if(length() == 0) {
      return IntVector.NA;
    } else {
      return getElementAsInt(0);
    }
  }

  @Override
  public double[] toDoubleArray() {
    if(Profiler.ENABLED) {
      Profiler.memoryAllocated(Double.SIZE, length());
    }
    double [] d = new double[length()];
    for(int i=0;i!=d.length;++i) {
      d[i] = getElementAsDouble(i);
    }
    return d;
  }

  @Override
  public String[] toStringArray() {
    String array[] = new String[length()];
    for (int i = 0; i < length(); i++) {
      array[i] = getElementAsString(i);
    }
    return array;
  }

  @Override
  public int[] toIntArray() {
    if(Profiler.ENABLED) {
      Profiler.memoryAllocated(Integer.SIZE, length());
    }
    int[] array = new int[length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = getElementAsInt(i);
    }
    return array;
  }

  @Override
  public final boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(!(obj instanceof AtomicVector)) {
      return false;
    }
    AtomicVector other = (AtomicVector) obj;
    if(this.length() != other.length()) {
      return false;
    }
    if(!this.getVectorType().equals(other.getVectorType())) {
      return false;
    }
    if(!this.getAttributes().equals(other.getAttributes())) {
      return false;
    }
    Vector.Type vectorType = getVectorType();
    for (int i = 0; i < length(); i++) {
      if(!vectorType.elementsIdentical(this, i, other, i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final boolean isWiderThan(Vector vector) {
    return getVectorType().isWiderThan(vector.getVectorType());
  }

  @Override
  public Logical getElementAsLogical(int index) {
    return Logical.valueOf(getElementAsRawLogical(index));
  }

  private class Elements implements Iterable<SEXP> {
    @Override
    public Iterator<SEXP> iterator() {
      return new ElementIterator();
    }
  }

  private class ElementIterator extends UnmodifiableIterator<SEXP> {
    private int index = 0;

    @Override
    public boolean hasNext() {
      return index < length();
    }

    @Override
    public SEXP next() {
      return getElementAsSEXP(index++);
    }
  }

  protected abstract static class AbstractAtomicBuilder extends AbstractBuilder<Vector> {

    @Override
    public Builder set(int destinationIndex, SEXP exp) {
      if(!(exp instanceof AtomicVector) || exp.length() != 1) {
        throw new IllegalArgumentException("the argument must be an atomic vector of length 1");
      }
      setFrom(destinationIndex, (AtomicVector) exp, 0);
      return this;
    }

    @Override
    public Builder add(SEXP exp) {
      if(!(exp instanceof AtomicVector) || exp.length() != 1) {
        throw new IllegalArgumentException("the argument must be an atomic vector of length 1");
      }
      addFrom((AtomicVector) exp, 0);
      return this;
    }
  }
}
