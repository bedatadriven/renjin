/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.ir;

import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;
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

  /**
   * The length of this value, or {@code UNKNOWN_LENGTH} if not known or known to vary.
   */
  private int length = UNKNOWN_LENGTH;
  
  /**
   * The bit set of this value's possible types.
   */
  private int typeSet = TypeSet.ANY_TYPE;

  /**
   * This value, if constant. {@code null} if unknown or known to vary.
   */
  private SEXP constantValue = null;

  /**
   * This value's dim attribute, or {@code null} if the dim attribute is unknown or not constant.
   */
  private AtomicVector constantDimAttribute = null;

  /**
   * This value's class attribute, or {@code null} if the class attribute is unknown or not constant.
   */
  private AtomicVector constantClassAttribute = null;
  
  /**
   * This value's attribute if constant, or {@code null} if the value's attributes are unknown or known to vary.
   */
  private AttributeMap constantAttributes = null;
  
  
  private ValueBounds() {};
  
  private ValueBounds(ValueBounds toCopy) {
    this.length = toCopy.length;
    this.typeSet = toCopy.typeSet;
    this.constantValue = toCopy.constantValue;
    this.constantAttributes = toCopy.constantAttributes;
    this.constantClassAttribute = toCopy.constantClassAttribute;
    this.constantDimAttribute = toCopy.constantDimAttribute;
  }

  /**
   * Constructs a {@code ValueBounds} for a scalar value of a known type with no attributes.
   */
  public static ValueBounds primitive(int type) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.typeSet = type;
    valueBounds.length = SCALAR_LENGTH;
    valueBounds.constantClassAttribute = Null.INSTANCE;
    valueBounds.constantDimAttribute = Null.INSTANCE;
    valueBounds.constantAttributes = AttributeMap.EMPTY;
    return valueBounds;
  }

  /**
   * Constructs a {@code ValueBounds} for a constant {@code SEXP}, that is, an {@code SEXP} that we
   */
  public static ValueBounds of(SEXP value) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.constantValue = value;
    valueBounds.typeSet = TypeSet.of(value);
    valueBounds.length = value.length();
    valueBounds.constantClassAttribute = value.getAttributes().getClassVector();
    valueBounds.constantDimAttribute = value.getAttributes().getDim();
    valueBounds.constantAttributes = value.getAttributes();
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
  
  public ValueBounds withAttributes(AttributeMap attributes) {
    ValueBounds bounds = new ValueBounds(this);
    bounds.constantAttributes = attributes;
    bounds.constantClassAttribute = attributes.getClassVector();
    bounds.constantDimAttribute = attributes.getDim();
    return bounds;
  }
  
  public boolean isConstant() {
    return constantValue != null;
  }
  
  public boolean isConstant(Symbol symbol) {
    return constantValue == symbol;
  }

  public boolean isLengthConstant() {
    return length != UNKNOWN_LENGTH;
  }
  
  public boolean isClassAttributeConstant() {
    return constantClassAttribute != null;
  }

  public boolean isDimAttributeConstant() { return constantDimAttribute != null; }

  public boolean isDimCountConstant() {
    return constantDimAttribute != null;
  }
  
  public boolean isAttributeConstant() {
    return constantAttributes != null;
  }

  public AtomicVector getConstantClassAttribute() {
    if(constantClassAttribute == null) {
      throw new IllegalStateException("class attribute is not constant");
    }
    return constantClassAttribute;
  }
  
  public AtomicVector getConstantDimAttribute() {
    if(constantDimAttribute == null) {
      throw new IllegalStateException("dim attribute is not constant");
    }
    return constantDimAttribute;
  }

  public int getConstantDimCount() {
    if(constantDimAttribute == null) {
      throw new IllegalStateException("dim attribute is not constant");
    }
    return constantDimAttribute.length();
  }

  public AttributeMap getConstantAttributes() {
    if(constantAttributes == null) {
      throw new IllegalArgumentException("attributes are not constant");
    }
    return constantAttributes;
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
    u.constantClassAttribute = unionConstant(this.constantClassAttribute, other.constantClassAttribute);
    u.constantDimAttribute = unionConstant(this.constantDimAttribute, other.constantDimAttribute);
    u.constantAttributes = unionConstant(this.constantAttributes, other.constantAttributes);
    
    return u;
  }


  private static int unionLengths(int x, int y) {
    if(x == y) {
      return x;
    }
    return UNKNOWN_LENGTH;
  }

  private <T> T unionConstant(T x, T y) {
    // If either class(x) is unknown, or class(y) is unknown, then
    // their union is also unknown
    if(x == null || y == null) {
      return null;
    }
    
    // If they have the same class, then their unknown is null
    if(x.equals(y)) {
      return x;
    }
    
    // Otherwise we don't know what the class will be.
    return null;
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
      return "[const " + formatConstant(constantValue) + "]";
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
    
    if(constantAttributes == null) {
      if(constantClassAttribute == null) {
        s.append(", class=?");
      } else if(constantClassAttribute == Null.INSTANCE) {
        s.append(", class=∅");
      } else {
        s.append(", class={").append(constantClassAttribute).append("}");
      }
    } else if(constantAttributes == AttributeMap.EMPTY) {
      s.append(", attr=∅");
    } else {
      s.append(", attr={");
      appendAttributesTo(s, constantAttributes);
      s.append("}");
    }
    
    s.append("]");
    
    return s.toString();
  }

  private String formatConstant(SEXP constantValue) {
    StringBuilder s = new StringBuilder(formatConstantValue(constantValue));
    AttributeMap attributes = constantValue.getAttributes();
    appendAttributesTo(s, attributes);
    return s.toString();
  }

  private void appendAttributesTo(StringBuilder s, AttributeMap attributes) {
    if(attributes != AttributeMap.EMPTY) {
      for (PairList.Node node : attributes.nodes()) {
        s.append(", ").append(node.getTag().getPrintName()).append("=").append(formatConstant(node.getValue()));
      }
    }
  }

  private String formatConstantValue(SEXP constantValue) {
    if(constantValue.length() == 1) {
      if(constantValue instanceof IntVector) {
        return ((IntVector) constantValue).getElementAsInt(0) + "L";
      } else if(constantValue instanceof DoubleVector) {
        return Double.toString(((DoubleVector) constantValue).getElementAsDouble(0));
      }
    }
    if(constantValue instanceof IntSequence) {
      IntSequence seq = (IntSequence) constantValue;
      if(seq.getBy() == 1) {
        return seq.getFrom() + ":" + (seq.getFrom() + seq.getLength());
      }
    }
    return constantValue.toString();
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
}
