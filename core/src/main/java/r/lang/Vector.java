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

import org.apache.commons.math.complex.Complex;

/**
 * Provides a common interface to {@code ListExp}, all {@code AtomicExp}s, and
 * {@code PairList}s
 */
public interface Vector extends SEXP {

  int getIndexByName(String name);

  /**
   * @param index zero-based index of the element
   * @return the element at {@code index} as a {@link SEXP},
   * wrapping the element if necessary in a new {@link AtomicVector} if necessary
   */
  SEXP getElementAsSEXP(int index);

  double getElementAsDouble(int index);

  int getElementAsInt(int index);

  String getElementAsString(int index);

  Logical getElementAsLogical(int index);

  Complex getElementAsComplex(int index);


  /**
   * Returns a builder for this type, initially empty.
   * @param initialSize
   * @return
   */
  Builder newBuilder(int initialSize);

  Type getVectorType();


  /**
   *
   * @param vector
   * @return true if this vector can be widened to the given
   * vector
   */
  boolean isWiderThan(Vector vector);

  /**
   *         
   * @return a builder initialized with a copy of this set of elements.
   */
  Builder newCopyBuilder();

  /**
   *
   * @param index zero-based index
   * @return  true if the element at {@code index} is NA (statistically missing)
   */
  boolean isElementNA(int index);

  public static interface Builder<S extends SEXP> {

    /**
     * Sets the element at index {@code index} to NA
     */
    Builder setNA(int index);

    public Builder setFrom(int destinationIndex, S source, int sourceIndex );

    public Vector build();
  }

  static class Order {
    // NULL < raw < logical < integer < double < complex < character < list < expression
    // these
    public static final int NULL = 0;
    public static final int RAW = 1;
    public static final int LOGICAL = 2;
    public static final int INTEGER = 3;
    public static final int DOUBLE = 4;
    public static final int COMPLEX = 5;
    public static final int CHARACTER = 6;
    public static final int LIST = 7;
    public static final int EXPRESSION = 8;
  }

  public static abstract class Type implements Comparable<Type> {
    private final int size;

    protected Type(int size) {
      this.size = size;
    }

    @Override
    public int compareTo(Type o) {
      return size - o.size;
    }

    public abstract Builder newBuilder();

    public final boolean isAtomic() {
      return size < Order.LIST;
    }

    public final boolean isWiderThan(Type type) {
      return size > type.size;
    }

    public final boolean isWiderThan(Vector vector) {
      return isWiderThan(vector.getVectorType());
    }

    public static Type widest(Type a, Type b) {
      if(b.isWiderThan(a)) {
        return b;
      } else {
        return a;
      }
    }

    public static Type widest(Type a, Vector b) {
      return widest(a, b.getVectorType());
    }
  }
}
