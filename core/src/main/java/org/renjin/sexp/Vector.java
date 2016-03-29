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

package org.renjin.sexp;

import org.apache.commons.math.complex.Complex;

/**
 * Provides a common interface to {@code ListExp}, all {@code AtomicExp}s, and
 * {@code PairList}s
 */
public interface Vector extends SEXP {

  /**
   * Set to true at build time to get messages
   * when Renjin allocates/copies new big vectors
   */
  public static final boolean DEBUG_ALLOC = false;

  /**
   *
   * @param index zero-based index
   * @return the element at {@code index} as a double value, converting if necessary. If no conversion is
   * possible,
   */
  double getElementAsDouble(int index);

  /**
   *
   * @param index zero-based index
   * @return the element at {@code index} as an {@code int} value, converting if necessary. If no conversion
   * is possible, {@link IntArrayVector#NA}
   */
  int getElementAsInt(int index);
  
  /**
   *
   * @param index zero-based index
   * @return the element at {@code index} as a {@code String} value
   */
  String getElementAsString(int index);

  /**
   * @param index  zero-based index
   * @return the element at {@code index} as a {@link Logical} value
   */
  Logical getElementAsLogical(int index);

  /**
   * @param index zero-based index
   * @return the element at {@code index} as logical value, encoded as an integer. See {@link Logical#internalValue}
   */
  int getElementAsRawLogical(int index);
  
  /**
   * @param index zero-based index
   * @return true if the element at {@code index} is true.
   */
  boolean isElementTrue(int index);

  /**
   *
   * @param index zero-based index
   * @return the element at {@code index} as an unsigned byte. See {@link com.google.common.primitives.UnsignedBytes}
   * for utilities for working with unsigned bytes.
   */
  byte getElementAsByte(int index);
  
  /**
  *
  * @param vector an {@code AtomicVector}
  * @param vectorIndex an index of {@code vector}
  * @param startIndex
  * @return the index of the first element in this vector that equals
  * the element at {@code vectorIndex} in {@code vector}, or -1 if no such element
  * can be found
  */
 int indexOf(Vector vector, int vectorIndex, int startIndex);
  

 /**
  * @param vector an {@code AtomicVector }
  * @param vectorIndex an index of {@code vector}
  * @return true if this vector contains an element equal to the
  * the element at {@code vectorIndex} in {@code vector}
  */
  boolean contains(Vector vector, int vectorIndex);
 
  /**
   *
   * @param index zero-based index
   * @return  the element at {@code index} as a {@link Complex} value
   */
  Complex getElementAsComplex(int index);

  /**
   * Returns a builder for this type of vector, with an initial number
   * of elements with the value {@code NA}.
   * 
   * @param initialSize the number of elements with which to initially populate the vector
   * @return
   */
  Builder newBuilderWithInitialSize(int initialSize);
 
  /**
   * 
   * Returns a builder for this type of vector, initially allocating enough
   * capacity for the given number of elements, but setting the initial size
   * to zero.
   * 
   * @param initialCapacity
   * @return
   */
  Builder newBuilderWithInitialCapacity(int initialCapacity);
  
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
   * Creates a new Builder which is initialized with all of this vector's elements
   * AND its attributes.
   *
   * @return a builder initialized with a copy of this set of elements.
   */
  Builder newCopyBuilder();

  /**
   * Creates a new Builder which is initialized with all of this vector's elements 
   * AND it's attributes. If the given {@code type} is wider than this vector's type,
   * then that type is used.
   * 
   * @param type
   * @return
   */
  Builder newCopyBuilder(Vector.Type type);

  /**
   * Checks whether the element is the NA value for this type. Note that this method
   * will return {@code false} for double {@code NaN} values.
   *
   * @param index zero-based index
   * @return  true if the element at {@code index} is NA (statistically missing), false if otherwise. 
   */
  boolean isElementNA(int index);

  /**
   * 
   * @param index zero-based index
   * @return true if the element at {@code index} is Not a Number (NaN), including values 
   * which are NA (statistically missing)
   */
  boolean isElementNaN(int index);



  /**
   * @return true if elements of this vector can be accessed in time constant
   * with regard to the length of the vector
   */
  boolean isConstantAccessTime();

  /**
   * Returns the element at index {@code index} of the vector as a native
   * JVM object, depending on the underlying R type:
   *
   * <ul>
   * <li>logical: java.lang.Boolean</li>
   * <li>integer: java.lang.Integer</li>
   * <li>double: java.lang.Double</li>
   * <li>complex: org.apache.commons.math.complex.Complex</li>
   * <li>character: java.lang.String</li>
   * </ul>
   *
   * @param index
   * @return
   * @throws IllegalArgumentException if the index is out of bounds or
   * the element at {@code index} is NA.
   */
  Object getElementAsObject(int index);

  int getComputationDepth();


  /**
   * An interface to
   * @param <S>
   */
  public static interface Builder<S extends SEXP>  extends SEXPBuilder {

    /**
     * Sets the element at index {@code index} to {@code NA}.
     * If the vector under construction is not long enough, it is lengthened.
     */
    Builder setNA(int index);

    /**
     * Adds a new {@code NA} element to the end of the vector under construction
     * @return this Builder, for method chaining
     */
    Builder addNA();

    /**
     * Reads the element at {@code sourceIndex} from the {@code source} expression and
     * adds a new {@code NA} element to the end of the vector under construction.
     *
     * @param source
     * @param sourceIndex
     * @return this Builder, for method chaining
     */
    Builder addFrom(S source, int sourceIndex);

    /**
     * Reads the element at {@code sourceIndex} from the {@code source} expression and
     * replaces the element at {@code destinationIndex} in the vector under construction.
     * If the vector under construction is not long enough, it is lengthened.
     * @param destinationIndex the index
     * @param source
     * @param sourceIndex
     * @return
     */
    Builder setFrom(int destinationIndex, S source, int sourceIndex );

    /**
     * Sets the element at {@code destinationIndex} to the expression {@code exp}.
     * if
     *
     * @param destinationIndex the index of the element to set
     * @param exp the value with which to replace the element
     * @throws IllegalArgumentException if this is an {@link AtomicVector.Builder} and {@code exp} is not
     * an {@link AtomicVector} of length 1.
     * @return
     */
    Builder set(int destinationIndex, SEXP exp);

    Builder add(SEXP exp);

    Builder add(Number value);
    
    /**
     *
     * @param name  the name of the attribute
     * @param value  the value of the attribute
     * @return this Builder, for method chaining
     */
    Builder setAttribute(String name, SEXP value);

    /**
    *
    * @param name  the name of the attribute
    * @param value  the value of the attribute
    * @return this Builder, for method chaining
    */
    Builder setAttribute(Symbol name, SEXP value);
    
    Builder removeAttribute(Symbol name);

    Builder setDim(int row, int col);

    SEXP getAttribute(Symbol install);

    /**
     * @return the current length of the vector under construction.
     */
    int length();

    /**
     * @return a new Vector.
     */
    Vector build();
    

    /**
     * Copies attributes from the provided {@code vector} argument, replacing 
     * an attribute if it is already set, or adding a new one if no previous 
     * value has been set.
     * 
     * @param vector the {@code Vector} from which to copy the attributes
     */
    Builder copyAttributesFrom(SEXP vector);

    Builder copySomeAttributesFrom(SEXP exp, Symbol... toCopy);
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

    public abstract Builder newBuilderWithInitialSize(int initialSize);
    
    /**
     * Returns a builder for this type of vector, with no elements added, but
     * an initial capacity of {@code initialCapacity}

     */
    public abstract Builder newBuilderWithInitialCapacity(int initialCapacity);
    
    public final boolean isAtomic() {
      return size < Order.LIST;
    }

    /**
     * Returns the narrowest {@code Vector.Type} that can contain {@code element}.
     * For example, for a {@code DoubleVector}, this will be {@code DoubleVector.VECTOR_TYPE}, but
     * for an {@code Environment}, this will be {@code ListVector.VECTOR_TYPE}
     *
     * @param element an {@code SEXP} to be added to a {@code Vector}
     * @return the narrowest {@code Vector.Type} that can contain {@code element}.
     */
    public static Type forElement(SEXP element) {
      if(element instanceof AtomicVector) {
        return ((AtomicVector) element).getVectorType();
      } else {
        return ListVector.VECTOR_TYPE;
      }

    }

    public final boolean isWiderThan(Type type) {
      return size > type.size;
    }

    public final boolean isWiderThan(Vector vector) {
      return isWiderThan(vector.getVectorType());
    }

    public final boolean isWiderThanOrEqualTo(Vector vector) {
      return compareTo(vector.getVectorType()) >= 0;
    }
    
    public final boolean isWiderThanOrEqualTo(Vector.Type vectorType) {
      return compareTo(vectorType) >= 0;
    }
    

    /**
     * Creates a new {@code Vector} of this {@code Type} from the element at
     * {@code index} in vector.
     * @param vector
     * @param index
     * @return
     */
    public abstract Vector getElementAsVector(Vector vector, int index);

    /**
     * Compares the two elements, coercing types to this {@code Type}.
     * @param vector1
     * @param index1
     * @param vector2
     * @param index2
     * @throws IllegalArgumentException if either of the two elements is NA or NaN
     * @return
     */
    public abstract int compareElements(Vector vector1, int index1, Vector vector2, int index2);

    
    /**
     * Checks equality between the two elements, coercing types to this {@code Type}. If either
     * of the two elements is NA, it will return false.
     * @param vector1
     * @param index1
     * @param vector2
     * @param index2
     * @return
     */    
    public abstract boolean elementsEqual(Vector vector1, int index1, Vector vector2, int index2);

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

    public static Type widest(Vector vector, SEXP element) {
      return widest(vector.getVectorType(), forElement(element));
    }
  }
}
