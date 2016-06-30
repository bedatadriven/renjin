package org.renjin.compiler.ir;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.sexp.*;

import java.util.Iterator;

/**
 * Describes the bounds of known types for an expression.
 */
public class ValueBounds {
  
  public static final int UNKNOWN_LENGTH = -1;
  public static final int SCALAR_LENGTH = 1;
  
  public static final ValueBounds UNBOUNDED = new ValueBounds();
  
  public static final ValueBounds INT_PRIMITIVE = primitive(TypeSet.INT);
  
  public static final ValueBounds DOUBLE_PRIMITIVE = primitive(TypeSet.DOUBLE);

  public static final ValueBounds LOGICAL_PRIMITIVE = primitive(TypeSet.LOGICAL);

  private int length = UNKNOWN_LENGTH;
  private int typeSet = TypeSet.ANY_TYPE;
  
  private SEXP constantValue = null;
  
  private ValueBounds() {};

  public static ValueBounds primitive(int type) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.typeSet = type;
    valueBounds.length = SCALAR_LENGTH;
    return valueBounds;
  }

  public static ValueBounds of(SEXP value) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.constantValue = value;
    valueBounds.typeSet = TypeSet.of(value);
    valueBounds.length = value.length();
    return valueBounds;
  }


  public ValueBounds of(Object value) {
    if(value instanceof SEXP) {
      return of((SEXP)value);
    } else if(value instanceof Integer) {
      return of(IntVector.valueOf((Integer) value));
    } else if(value instanceof Double) {
      return of(DoubleVector.valueOf((Double) value));
    }
    throw new UnsupportedOperationException("value: " + value);
  }


  public static ValueBounds of(Class returnType) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.typeSet = TypeSet.of(returnType);
    if(returnType.isPrimitive()) {
      valueBounds.length = 1;
    } else {
      valueBounds.length = UNKNOWN_LENGTH;
    }
    return valueBounds;
  }
  
  public boolean isConstant() {
    return constantValue != null;
  }
  
  /**
   * @return a bit mask indicating which R types are included in this bounds.
   */
  public int getTypeSet() {
    return typeSet;
  }

  public int getLength() {
    return length;
  }

  /**
   * @return a new {@code TypeBounds} that is the union of this bounds and the {@code other}
   */
  public ValueBounds union(ValueBounds other) {
    ValueBounds u = new ValueBounds();
    u.typeSet = this.typeSet | other.typeSet;
    u.length = unionLengths(this.length, other.length);
    return u;
  }

  private static int unionLengths(int x, int y) {
    if(x == y) {
      return x;
    }
    return UNKNOWN_LENGTH;
  }

  /**
   * @return a new {@code TypeBounds} that is the union of the given {@code bounds}. 
   * @throws IllegalArgumentException if {@code bounds} is emtpy.
   */
  public static ValueBounds union(Iterable<ValueBounds> bounds) {
    Iterator<ValueBounds> it = bounds.iterator();
    Preconditions.checkArgument(it.hasNext());
    
    ValueBounds union = it.next();
    
    while(it.hasNext()) {
      union = union.union(it.next());
    }
    return union;
  }

  /**
   * @return a new {@code TypeBounds} that includes vectors of the given type, with
   * unbound length and <strong>no class attribute.</strong>
   */
  public static ValueBounds vector(int types) {
    ValueBounds bounds = new ValueBounds();
    bounds.typeSet = types;
    bounds.length = UNKNOWN_LENGTH;
    return bounds;
  }

  public static ValueBounds vector(Class type) {
    return vector(TypeSet.of(type));
  }

  public static ValueBounds vector(Class type, int length) {
    ValueBounds bounds = new ValueBounds();
    bounds.typeSet = TypeSet.of(type);
    bounds.length = length;
    return bounds;
  }

  public static ValueBounds vector(int typeSet, int length) {
    ValueBounds bounds = new ValueBounds();
    bounds.typeSet = typeSet;
    bounds.length = length;
    return bounds;
  }
  
  public Type storageType() {
    if(typeSet == TypeSet.DOUBLE) {
      if(length == 1) {
        return Type.DOUBLE_TYPE;
      } else {
        return Type.getType(DoubleVector.class);
      }
    } else if(typeSet == TypeSet.INT ||
        typeSet == TypeSet.LOGICAL) {
      if(length == 1) {
        return Type.INT_TYPE;
      } else {
        return Type.getType(IntVector.class);
      }
    } else if(typeSet == TypeSet.RAW) {
      if(length == 1) {
        return Type.BYTE_TYPE;
      } else {
        return Type.getType(RawVector.class);
      }
    } else if(typeSet == TypeSet.STRING) {
      if (length == 1) {
        return Type.getType(String.class);
      } else {
        return Type.getType(StringVector.class);
      }
    } else {
      return Type.getType(SEXP.class);
    }
  }

  @Override
  public String toString() {
    
    if(isConstant()) {
      return "[" + constantValue + "]";
    }
    
    if(typeSet == TypeSet.ANY_TYPE && length == UNKNOWN_LENGTH) {
      return "[*]";
    }
    
    StringBuilder s = new StringBuilder();
    s.append("[");
    s.append(TypeSet.toString(typeSet));
    s.append(", len=");
    if(length == UNKNOWN_LENGTH) {
      s.append("*");
    } else {
      s.append(length);
    } 
    s.append("]");
    
    return s.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ValueBounds that = (ValueBounds) o;

    return length == that.length && typeSet == that.typeSet;
  }

  @Override
  public int hashCode() {
    int result = length;
    result = 31 * result + typeSet;
    return result;
  }

  public SEXP getConstantValue() {
    return constantValue;
  }


  public static boolean allConstant(Iterable<ValueBounds> argumentTypes) {
    for (ValueBounds argumentType : argumentTypes) {
      if(!argumentType.isConstant()) {
        return false;
      }
    }
    return true;
  }

  public boolean isLengthConstant() {
    return length != UNKNOWN_LENGTH;
  }
}
