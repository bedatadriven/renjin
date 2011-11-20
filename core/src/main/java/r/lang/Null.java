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

import r.lang.Vector.Builder;
import r.lang.exception.EvalException;

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
public final class Null extends AbstractSEXP implements AtomicVector, PairList, PromisePairList {

  public static final String TYPE_NAME = "NULL";

  public static final Null INSTANCE = new Null();
  public static final Vector.Type VECTOR_TYPE = new NullType();

  private static final String INDEX_OUT_OF_BOUNDS = "The NULL object is zero-length.";

  private Null() {
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
  public EvalResult evaluate(Context context, Environment rho) {
    return new EvalResult(this);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public <S extends SEXP> S getElementAsSEXP(int i) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
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
  public Iterable<PairList.Node> nodes() {
    return Collections.emptySet();
  }

  /**
   * Null implementation of {@link PairList#toVector()}
   * @return {@code Null.INSTANCE}
   */
  @Override
  public Null toVector() {
    return this;
  }

  /**
   * Null implementation of {@link r.lang.PairList#findByTag(Symbol)}
   * @param symbol the tag for which to search
   * @return {@code Null.INSTANCE}
   */
  @Override
  public SEXP findByTag(Symbol symbol) {
    return Null.INSTANCE;
  }

  @Override
  public StringVector getS3Class() {
    return new StringVector("NULL");
  }

  @Override
  public boolean isWiderThan(Vector vector) {
    return getVectorType().isWiderThan(vector);
  }

  @Override
  public SEXP setAttributes(ListVector attributes) {
    throw new EvalException("Attributes cannot be set on NULL");
  }

  @Override
  public PairList getAttributes() {
    return Null.INSTANCE;
  }
  
  

  @Override
  public SEXP setAttribute(String attributeName, SEXP value) {
    if(value == Null.INSTANCE) {
      return this;
    } else {
      throw new EvalException("Attributes cannot be set on NULL");
    }
  }

  @Override
  public Null getAttribute(Symbol name) {
    return Null.INSTANCE;
  }
  
  @Override
  public Null getNames() {
    return Null.INSTANCE;
  }

  @Override
  public Vector.Builder newCopyBuilder() {
    return NullBuilder.INSTANCE;
  }

  @Override
  public Vector.Builder newBuilderWithInitialSize(int initialSize) {
    if(initialSize == 0) {
      return NullBuilder.INSTANCE;
    } else {
      throw new UnsupportedOperationException("Cannot build NULL with non-zero length!");
    }
  }
  
  @Override
  public Vector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return NullBuilder.INSTANCE;
  }

  @Override
  public Comparable getElementAsObject(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public boolean isElementNA(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public boolean isElementTrue(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }
  
  @Override
  public Complex getElementAsComplex(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public Logical getElementAsLogical(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public String getElementAsString(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public int getElementAsInt(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public double getElementAsDouble(int index) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public boolean containsNA() {
    return false;
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    return -1;
  }

  @Override
  public boolean contains(AtomicVector vector, int vectorIndex) {
    return false;
  }

  @Override
  public SEXP getRawTag() {
    return Null.INSTANCE;
  }

  @Override
  public Symbol getTag() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasTag() {
    return false;
  }

  @Override
  public void setTag(SEXP tag) {
    if(tag != Null.INSTANCE) {
      throw new IllegalArgumentException("Null object is immutable");
    }
  }

  @Override
  public int indexOfNA() {
    return -1;
  }

  @Override
  public int compare(int index1, int index2) {
    throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public PairList clone() {
    return this;
  }

  @Override
  public double[] toDoubleArray() {
    return new double[0];
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  private static class NullBuilder implements Vector.Builder<SEXP> {

    public static final NullBuilder INSTANCE = new NullBuilder();

    public static final String NULL_IS_IMMUTABLE = "The NULL object is immutable";

    @Override
    public Vector.Builder setNA(int index) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder setFrom(int destinationIndex, SEXP source, int sourceIndex) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder copyAttributesFrom(Vector exp) {
      if(exp.getAttributes() != Null.INSTANCE) {
        throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
      }
      return this;
    }
    
    @Override
    public Vector.Builder copySomeAttributesFrom(Vector exp, Symbol... toCopy) {
      if(exp.getAttributes() != Null.INSTANCE) {
        throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
      }
      return this;
    }

    @Override
    public Vector.Builder addNA() {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder addFrom(SEXP source, int sourceIndex) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder set(int destinationIndex, SEXP exp) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder add(SEXP exp) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }
    
    @Override
    public Vector.Builder add(Number value) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder setAttribute(String name, SEXP value) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }

    @Override
    public Vector.Builder setAttribute(Symbol name, SEXP value) {
      throw new UnsupportedOperationException(NULL_IS_IMMUTABLE);
    }
    
    @Override
    public int length() {
      return 0;
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

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      throw new IllegalArgumentException(INDEX_OUT_OF_BOUNDS);
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      throw new UnsupportedOperationException();
    }

  }

}
