package org.renjin.compiler.ir;

import com.google.common.collect.ImmutableSet;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

/**
 * Describes the bounds of known types for an expression.
 */
public class TypeBounds {
  // Length flags
  public static final int SCALAR = (1);
  public static final int VECTOR = (1 << 1);

  // Type Flags
  public static final int LIST = (1 << 1);
  public static final int NULL = (1 << 2);
  public static final int INT = (1 << 3);
  public static final int DOUBLE = (1 << 4);
  public static final int LOGICAL = (1 << 5);
  public static final int STRING = (1 << 6);
  public static final int COMPLEX = (1 << 7);
  public static final int RAW = (1 << 8);
  public static final int SYMBOL = (1 << 9);
  public static final int FUNCTION = (1 << 10);
  public static final int ENVIRONMENT = (1 << 11);
  public static final int ANY_TYPE = 0xFFFFFFFF;
  
  
  public static final TypeBounds UNBOUNDED = new TypeBounds();
  
  public static final TypeBounds SCALAR_INT = scalar(INT);
  
  public static final TypeBounds SCALAR_DOUBLE = scalar(DOUBLE);

  private static final ImmutableSet<Integer> OPEN_LENGTH_SET = null;

  private static final ImmutableSet<Integer> SCALAR_LENGTH_SET = ImmutableSet.of(1);

  private static final ImmutableSet<SEXP> NULL_CLASS_SET = ImmutableSet.of((SEXP)Null.INSTANCE);

  private static final int OPEN_SET = 0xFFFFFFFF;
  

  /**
   * Bit set of possible SEXP types.
   */
  private int types = ANY_TYPE;

  /**
   * Set of possible class attributes. {@code null} if we have no information.
   */
  private ImmutableSet<SEXP> classAttributes = null;

  /**
   * Set of possible  
   */
  private ImmutableSet<Integer> lengths = OPEN_LENGTH_SET;
  
  private TypeBounds() {};

  public static TypeBounds openSet() {
    return UNBOUNDED;
  }
  
  private static TypeBounds scalar(int type) {
    TypeBounds bounds = new TypeBounds();
    bounds.types = type;
    bounds.lengths = SCALAR_LENGTH_SET;
    bounds.classAttributes = NULL_CLASS_SET;
    return bounds;
  }
  

  public static TypeBounds nullSexp() {
    return new TypeBounds(NULL);
  }

  public static TypeBounds scalarLogical() {
    return new TypeBounds(SCALAR | LOGICAL);
  }

  public TypeBounds union(TypeBounds typeBounds) {
    return new TypeBounds(this.bounds | typeBounds.bounds);
  }

  @Override
  public String toString() {
    if(bounds == OPEN_SET) {
      return "{*}";
    }
    StringBuilder s = new StringBuilder();
    appendTo(s, "SCALAR", SCALAR);
    appendTo(s, "VECTOR", VECTOR);
    appendTo(s, "INT", INT);
    appendTo(s, "DOUBLE", DOUBLE);
    appendTo(s, "STRING", STRING);
    appendTo(s, "LIST", LIST);
    appendTo(s, "NULL", NULL);
    return "{" + s + "}";
  }
  
  private void appendTo(StringBuilder sb, String name, int bit) {
    if( (bounds & bit) != 0) {
      if(sb.length() > 0) {
        sb.append(" | ");
      }
      sb.append(name);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypeBounds bounds1 = (TypeBounds) o;

    return bounds == bounds1.bounds;

  }

  @Override
  public int hashCode() {
    return bounds;
  }
}
