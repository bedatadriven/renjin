/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.math.complex.Complex;

import java.util.Iterator;

public abstract class AbstractAtomicVector extends AbstractVector implements AtomicVector{

  protected AbstractAtomicVector(PairList attributes) {
    super(attributes);
  }

  protected AbstractAtomicVector() {
  }

  @Override
  public Iterable<SEXP> elements() {
    return new Elements();
  }

  @Override
  public boolean containsNA() {
    return indexOfNA() != -1;
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
  public double[] toDoubleArray() {
    double [] d = new double[length()];
    for(int i=0;i!=d.length;++i) {
      d[i] = getElementAsDouble(i);
    }
    return d;
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
