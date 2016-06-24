package org.renjin.compiler.ir;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.renjin.sexp.*;

import java.util.Set;

/**
 * Describes the bounds of known types for an expression.
 */
public class TypeBounds {
  
  // Length flags

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
  public static final int PAIRLIST = (1 << 12);
  public static final int ANY_ATOMIC_VECTOR = NULL | RAW | INT | LOGICAL | DOUBLE | COMPLEX;
  public static final int ANY_VECTOR = LIST | ANY_ATOMIC_VECTOR;
  public static final int ANY_TYPE = ANY_VECTOR | PAIRLIST | ENVIRONMENT | SYMBOL | FUNCTION;
  
  
  public static final TypeBounds UNBOUNDED = new TypeBounds();
  
  public static final TypeBounds INT_PRIMITIVE = primitive(INT);
  
  public static final TypeBounds DOUBLE_PRIMITIVE = primitive(DOUBLE);

  public static final TypeBounds LOGICAL_PRIMITIVE = primitive(LOGICAL);

  private static final ImmutableSet<SEXP> NULL_CLASS_SET = ImmutableSet.of((SEXP)Null.INSTANCE);


  /**
   * Bit set of possible SEXP types.
   */
  private int types = ANY_TYPE;

  
  /**
   * Set of possible class attributes. {@code null} if we have no information.
   */
  private Set<SEXP> classAttributes = null;

  /**
   * Set of possible  
   */
  private Set<Integer> lengths = null;
  
  private TypeBounds() {};

  public static TypeBounds openSet() {
    return UNBOUNDED;
  }
  
  
  public static TypeBounds primitive(int type) {
    TypeBounds bounds = new TypeBounds();
    bounds.types = type;
    bounds.lengths = ImmutableSet.of(1);
    bounds.classAttributes = NULL_CLASS_SET;
    return bounds;
  }
  
  public static TypeBounds nullSexp() {
    TypeBounds bounds = new TypeBounds();
    bounds.types = NULL;
    bounds.lengths = ImmutableSet.of(0);
    bounds.classAttributes = ImmutableSet.of((SEXP)Null.INSTANCE);
    return bounds;
  }
  
  public static TypeBounds of(SEXP constant) {
    TypeBounds bounds = new TypeBounds();
    if(constant instanceof ListVector) {
      bounds.types = LIST;
    } else if(constant instanceof Null) {
      bounds.types = NULL;
    } else if(constant instanceof LogicalVector) {
      bounds.types = LOGICAL;
    } else if(constant instanceof RawVector) {
      bounds.types = RAW;
    } else if(constant instanceof IntVector) {
      bounds.types = INT;
    } else if(constant instanceof DoubleVector) {
      bounds.types = DOUBLE;
    } else if(constant instanceof ComplexVector) {
      bounds.types = COMPLEX;
    } else if(constant instanceof StringVector) {
      bounds.types = STRING;
    } else if(constant instanceof Symbol) {
      bounds.types = SYMBOL;
    } else if(constant instanceof Environment) {
      bounds.types = ENVIRONMENT;
    } else if(constant instanceof PairList) {
      bounds.types = PAIRLIST;
    } else {
      throw new UnsupportedOperationException("TODO: " + constant.getClass().getName());
    }
    
    bounds.lengths = ImmutableSet.of(constant.length());
    bounds.classAttributes = ImmutableSet.of(constant.getAttributes().getClassVector());
    return bounds;
  }

  public int getTypes() {
    return types;
  }
  
  
  
  public TypeBounds union(TypeBounds other) {
    TypeBounds u = new TypeBounds();
    u.types = this.types | other.types;
    u.lengths = union(this.lengths, other.lengths);
    u.classAttributes = union(this.classAttributes, other.classAttributes);
    
    return u;
  }

  private static <T> Set<T> union(Set<T> a, Set<T> b) {
    if(a == null || b == null) {
      return null;
    }
    return Sets.union(a, b);
  }

  @Override
  public String toString() {
    
    if(types == ANY_TYPE && lengths == null) {
      return "[*]";
    }
    
    StringBuilder s = new StringBuilder();
    s.append("[");
    
    if( types == ANY_TYPE) {
      s.append("*");
    } else {
      appendType(s, "list", LIST);
      appendType(s, "null", NULL);
      appendType(s, "int", INT);
      appendType(s, "double", DOUBLE);
      appendType(s, "logical", LOGICAL);
      appendType(s, "character", STRING);
      appendType(s, "complex", COMPLEX);
      appendType(s, "raw", RAW);
      appendType(s, "symbol", SYMBOL);
      appendType(s, "function", FUNCTION);
      appendType(s, "environment", ENVIRONMENT);
      appendType(s, "pairlist", PAIRLIST);
    }

    s.append(", len=");
    if(lengths == null) {
      s.append("*");
    } else if(lengths.size() == 1) {
      s.append(lengths.iterator().next());
    } else {
      s.append("{");
      Joiner.on(", ").appendTo(s, lengths);
      s.append("}");
    }
    s.append("]");
    
    return s.toString();
  }
  
  private void appendType(StringBuilder sb, String name, int bit) {
    if( (types & bit) != 0) {
      if(sb.length() > 1) {
        sb.append(" | ");
      }
      sb.append(name);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypeBounds bounds = (TypeBounds) o;

    if (types != bounds.types) return false;
    if (classAttributes != null ? !classAttributes.equals(bounds.classAttributes) : bounds.classAttributes != null)
      return false;
    return lengths != null ? lengths.equals(bounds.lengths) : bounds.lengths == null;

  }

  @Override
  public int hashCode() {
    int result = types;
    result = 31 * result + (classAttributes != null ? classAttributes.hashCode() : 0);
    result = 31 * result + (lengths != null ? lengths.hashCode() : 0);
    return result;
  }

  public static TypeBounds vector(int types) {
    TypeBounds bounds = new TypeBounds();
    bounds.types = types;
    bounds.lengths = null;
    bounds.classAttributes = NULL_CLASS_SET;
    return bounds;
  }
}
