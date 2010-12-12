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

import com.google.common.collect.Iterators;
import org.apache.commons.math.complex.Complex;

import java.util.Arrays;
import java.util.Iterator;

public class ComplexVector extends AbstractAtomicVector implements Iterable<Complex> {

  private final Complex[] values;
  public static final int TYPE_CODE = 15;
  public static final String TYPE_NAME = "complex";

  public static Vector.Type VECTOR_TYPE = new ComplexType();

  public static final Complex NA = new Complex(DoubleVector.NA, 0);

  public ComplexVector(Complex... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
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
  public Complex getElementAsObject(int index) {
    return values[index];
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
    throw new UnsupportedOperationException("implement me");
  }

  @Override
  public Logical getElementAsLogical(int index) {
    double value = values[index].getReal();
    return DoubleVector.isNA(value) ? Logical.NA : Logical.valueOf(value != 0);
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return values[index];
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex) {
    Complex value = vector.getElementAsComplex(vectorIndex);
    for(int i=0;i!=values.length;++i) {
      if(values[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Builder newBuilder(int initialSize) {
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
    throw new UnsupportedOperationException("implement me!");
  }

  private static class ComplexType extends Vector.Type {
    public ComplexType() {
      super(Order.COMPLEX);
    }

    @Override
    public Builder newBuilder() {
      throw new UnsupportedOperationException("implement me!");
    }
  }
}
