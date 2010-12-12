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

import org.apache.commons.math.complex.Complex;

import java.util.Collections;

/**
 * The R Nullary object.
 *
 * <p>
 * Null in R is quite different than an null pointer reference; the Null object
 * provides null implementations of the {@link SEXP}, {@link AtomicVector}, and
 * {@link PairList} interfaces.
 *
 * <p>
 * There is only one immutable instance of {@code Null} that can be referenced at
 * {@code Null.INSTANCE}
 */
public final class Null extends AbstractSEXP implements AtomicVector, PairList {

  public static final int TYPE_CODE = 0;
  public static final String TYPE_NAME = "NULL";

  public static final Null INSTANCE = new Null();
  public static final Vector.Type VECTOR_TYPE = new NullType();

  private static final IndexOutOfBoundsException INDEX_OUT_OF_BOUNDS_EXCEPTION =
      new IndexOutOfBoundsException("The NULL object is zero-length.");

  private Null() {  }

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
    return 0;
  }

  @Override
  public String toString() {
    return "NULL";
  }

  @Override
  public EvalResult evaluate(Environment rho) {
    return new EvalResult(this);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public <S extends SEXP> S get(int i) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  /**
   * Null implementation of {@link PairList#values()}
   *
   * @return an empty set
   */
  @Override
  public Iterable<SEXP> values() {
    return Collections.emptySet();
  }

  /**
   * Null implementation of {@link r.lang.PairList#nodes()}
   *
   * @return  an empty set
   */
  @Override
  public Iterable<Node> nodes() {
    return Collections.emptySet();
  }

  /**
   * Null implementation of {@link r.lang.PairList#findByTag(SymbolExp)}
   * @param symbol the tag for which to search
   * @return {@code Null.INSTANCE}
   */
  @Override
  public SEXP findByTag(SymbolExp symbol) {
    return Null.INSTANCE;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public Vector.Builder newBuilder(int initialSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWiderThan(Vector vector) {
    return getVectorType().isWiderThan(vector);
  }

  @Override
  public Vector.Builder newCopyBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getElementAsObject(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public boolean isElementNA(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public Complex getElementAsComplex(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public Logical getElementAsLogical(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public String getElementAsString(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public int getElementAsInt(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public double getElementAsDouble(int index) {
    throw INDEX_OUT_OF_BOUNDS_EXCEPTION;
  }

  @Override
  public boolean containsNA() {
    return false;
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex) {
    return -1;
  }

  @Override
  public boolean contains(AtomicVector vector, int vectorIndex) {
    return false;
  }

  @Override
  public int indexOfNA() {
    return -1;
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  private static class NullBuilder implements Vector.Builder {

    public static final NullBuilder INSTANCE = new NullBuilder();

    @Override
    public Vector.Builder setNA(int index) {
      throw new UnsupportedOperationException("values cannot be added to the NULL value");
    }

    @Override
    public Vector.Builder setFrom(int destinationIndex, SEXP source, int sourceIndex) {
      throw new UnsupportedOperationException("values cannot be added to the NULL value");
    }

    @Override
    public Null build() {
      return Null.INSTANCE;
    }
  }

  private static class NullType extends Vector.Type {

    public NullType() {
      super(Order.NULL);
    }

    @Override
    public Vector.Builder newBuilder() {
      return NullBuilder.INSTANCE;
    }
  }
}
