/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.primitives.Identical;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;

/**
 * Describes the bounds of known types for an expression.
 */
public class ValueBounds {
  
  public static final int UNKNOWN_LENGTH = -1;
  public static final int SCALAR_LENGTH = 1;

  /**
   * The value may have some elements which are NA
   */
  public static final int MAY_HAVE_NA = 0;


  /**
   * The values has no elements which are NAs
   */
  public static final int NO_NA = 1;


  public static final ValueBounds UNBOUNDED = new ValueBounds.Builder().build();
  
  public static final ValueBounds INT_PRIMITIVE = primitive(TypeSet.INT);
  
  public static final ValueBounds DOUBLE_PRIMITIVE = primitive(TypeSet.DOUBLE);

  public static final ValueBounds LOGICAL_PRIMITIVE = primitive(TypeSet.LOGICAL);
  
  /**
   * The length of this value, or {@code UNKNOWN_LENGTH} if not known or known to vary.
   */
  private int length = UNKNOWN_LENGTH;


  /**
   * Whether this value may have NA elements
   */
  private int na = MAY_HAVE_NA;
  
  /**
   * The bit set of this value's possible types.
   */
  private int typeSet = TypeSet.ANY_TYPE;

  /**
   * This value, if constant. {@code null} if unknown or known to vary.
   */
  private SEXP constantValue = null;


  /**
   * If true, the precise set of attributes defined for this value are not known, and attributes that 
   * are not defined in attributeBounds should be considered to have an unbounded value.
   */
  private boolean attributesOpen = true;

  
  /**
   * Map to known attributes. Each value will either be a SEXP not equal to Null.INSTANCE indicating that 
   * value is known to be constant, or {@code null} if this value is known to have this attribute, but the value of the
   * attribute is not known.
   */
  private Map<Symbol, SEXP> attributes;

  private ValueBounds() {}
  
  private ValueBounds(ValueBounds toCopy) {
    assert toCopy.attributes != null;
    this.length = toCopy.length;
    this.typeSet = toCopy.typeSet;
    this.na = toCopy.na;
    this.constantValue = toCopy.constantValue;
    this.attributes = toCopy.attributes;
    this.attributesOpen = toCopy.attributesOpen;
  }

  /**
   * Constructs a {@code ValueBounds} for a scalar value of a known type with no attributes
   * and not NA.
   */
  public static ValueBounds primitive(int type) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.typeSet = type;
    valueBounds.length = SCALAR_LENGTH;
    valueBounds.attributes = Collections.emptyMap();
    valueBounds.attributesOpen = false;
    valueBounds.na = NO_NA;
    return valueBounds;
  }

  /**
   * Constructs a {@code ValueBounds} for a constant {@code SEXP}, that is, an {@code SEXP} that we
   */
  public static ValueBounds of(SEXP value) {
    ValueBounds valueBounds = new ValueBounds();
    valueBounds.na = hasAnyNAs(value);
    valueBounds.constantValue = value;
    valueBounds.typeSet = TypeSet.of(value);
    valueBounds.length = value.length();
    valueBounds.attributes = value.getAttributes().toMap();
    valueBounds.attributesOpen = false;
    return valueBounds;
  }

  private static int hasAnyNAs(SEXP value) {
    if(value instanceof AtomicVector) {
      if(((AtomicVector) value).containsNA()) {
        return MAY_HAVE_NA;
      }
    }
    return NO_NA;
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
    valueBounds.attributesOpen = true;
    valueBounds.attributes = Collections.emptyMap();
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
  
  public boolean isConstant(Symbol symbol) {
    return constantValue == symbol;
  }

  public boolean isLengthConstant() {
    return length != UNKNOWN_LENGTH;
  }
  
  public boolean isAttributeConstant(Symbol name) {
    if(attributesOpen) {
      return attributes.get(name) != null;
    } else {
      // if the attribute set is closed, and this entry is missing
      // from the map, then we know the value is NULL
      if(!attributes.containsKey(name)) {
        return true;
      }
      return attributes.get(name) != null;
    }
  }


  public boolean isAttributeDefinitelyNull(Symbol name) {
    return getAttributeIfConstant(name) == Null.INSTANCE;
  }

  public SEXP getAttributeIfConstant(Symbol name) {
    if(attributesOpen) {
      return attributes.get(name);
    } else {
      // if the attribute set is closed, and this entry is missing
      // from the map, then we know the value is NULL
      if(!attributes.containsKey(name)) {
        return Null.INSTANCE;
      }
      return attributes.get(name);
    }
  }
  
  
  public boolean isClassAttributeConstant() {
    return isAttributeConstant(Symbols.CLASS);
  }

  public boolean isDimAttributeConstant() {
    return isAttributeConstant(Symbols.DIM);
  }

  public boolean isDimCountConstant() {
    return isDimAttributeConstant();
  }
  
  public boolean isAttributeConstant() {
    return !attributesOpen && !attributes.containsValue(null);
  }

  public boolean attributeCouldBePresent(Symbol attributeName) {

    // Is this attribute known to be absent?
    SEXP bounds = attributes.get(attributeName);
    if(bounds == Null.INSTANCE) {
      return false;
    }
    
    if(attributesOpen) {
      // Open set: could be present unless explicitly exluded above
      return true;

    } else {
      // Closed set: must have an entry in attributeBounds to be present
      return attributes.containsKey(attributeName);
    }
  }
  
  public AtomicVector getConstantClassAttribute() {
    return (AtomicVector) getAttributeIfConstant(Symbols.CLASS);
  }
  
  public AtomicVector getConstantDimAttribute() {
    return (AtomicVector) getAttributeIfConstant(Symbols.DIM);
  }

  public int getConstantDimCount() {
    return getConstantDimAttribute().length();
  }

  public Map<Symbol, SEXP> getAttributeBounds() {
    return attributes;
  }
  
  public boolean isAttributeSetOpen() {
    return attributesOpen;
  }

  /**
   * @return the value bounds of elements of this vector, as if from the
   * expression X[[i]]
   */
  public ValueBounds getElementBounds() {
    if(constantValue instanceof ListVector) {
      ListVector constantList = (ListVector) this.constantValue;
      List<ValueBounds> elementBounds = new ArrayList<>();
      for (int i = 0; i < constantList.length(); i++) {
        elementBounds.add(ValueBounds.of(constantList.getElementAsSEXP(i)));
      }
      return union(elementBounds);

    } else if(TypeSet.isDefinitelyAtomic(typeSet)) {
      return ValueBounds.primitive(typeSet);

    } else {
      return ValueBounds.UNBOUNDED;
    }
  }

  public AttributeMap getConstantAttributes() {
    
    if(attributesOpen) {
      throw new IllegalArgumentException("attribute set is not closed");
    }
    
    AttributeMap.Builder attributeMap = AttributeMap.builder();
    for (Map.Entry<Symbol, SEXP> entry : attributes.entrySet()) {
      assert entry.getValue() != null;
      attributeMap.set(entry.getKey(), entry.getValue());
    }
    
    return attributeMap.build();
  }
  
  /**
   * @return a bit mask indicating which R types are included in this bounds.
   */
  public int getTypeSet() {
    return typeSet;
  }

  public int getNA() {
    return na;
  }


  public boolean maybeNA() {
    return na == MAY_HAVE_NA;
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
    u.na = this.na & other.na;
    u.attributesOpen = this.attributesOpen || other.attributesOpen;
    
    if(this.attributes == other.attributes) {
      Preconditions.checkNotNull(attributes, "attributes");
      u.attributes = attributes;
    } else if(this.attributes.isEmpty() && other.attributes.isEmpty()) {
      u.attributes = Collections.emptyMap();
    } else {
      u.attributes = new HashMap<>();
      Set<Symbol> attributeNames = Sets.union(this.attributes.keySet(), other.attributes.keySet());
      for (Symbol attributeName : attributeNames) {
        SEXP thisValue = this.attributes.get(attributeName);
        SEXP otherValue = other.attributes.get(attributeName);

        u.attributes.put(attributeName, unionConstant(thisValue, otherValue));
      }
    }
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
    if(na == MAY_HAVE_NA) {
      s.append(", ?NA");
    }
    for (Map.Entry<Symbol, SEXP> attribute : attributes.entrySet()) {
      s.append(", ").append(attribute.getKey().getPrintName()).append("=");
      if(attribute.getValue() == null) {
        s.append("?");
      } else if(attribute.getValue() == Null.INSTANCE) {
        s.append("∅");
      } else {
        s.append("{").append(attribute.getValue()).append("}");
      }
    }
    if(attributesOpen) {
      s.append(", ...=?");
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

    return length == that.length && typeSet == that.typeSet &&
        this.attributesOpen == that.attributesOpen &&
        this.na == that.na &&
        Objects.equals(this.constantValue, that.constantValue) &&
        Objects.equals(this.attributes, that.attributes);
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

  /**
   * 
   * @return a new {@code ValueBounds} with the same type, length, and attributes, but non-constant values.
   */
  public ValueBounds withVaryingValues() {
    if(constantValue == null) {
      return this;
    }
    ValueBounds bounds = new ValueBounds();
    bounds.length = this.length;
    bounds.typeSet = this.typeSet;
    bounds.attributes = this.attributes;
    bounds.attributesOpen = this.attributesOpen;
    return this;
  }

  public static ValueBounds.Builder builder() {
    return new Builder();
  }

  /**
   * Returns true if the given {@code sexp} falls within these bounds.
   */
  public boolean test(SEXP sexp) {
    if(constantValue != null) {
      return Identical.identical(constantValue, sexp);
    }
    int sexpType = TypeSet.of(sexp);
    if((this.typeSet & sexpType) == 0) {
      return false;
    }
    if(this.length != UNKNOWN_LENGTH) {
      if(this.length != sexp.length()) {
        return false;
      }
    }
    if(this.na == NO_NA && sexp instanceof Vector) {
      Vector vector = (Vector) sexp;
      if(vector.anyNA()) {
        return false;
      }
    }

    // Fast attribute checks
    if (this.attributes.isEmpty()) {
      // No bounds on attributes
      if(attributesOpen) {
        return true;
      } else {
        return sexp.getAttributes().isEmpty();
      }
    }

    // Otherwise check
    AttributeMap attributes = sexp.getAttributes();
    for (Symbol symbol : attributes.names()) {
      SEXP expectedAttribute = this.attributes.get(symbol);
      if(expectedAttribute == null) {
        if(!attributesOpen) {
          return false;
        }
      } else {
        if (!Identical.identical(expectedAttribute, attributes.get(symbol))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns true if this ValueBounds is either an integer or double scalar, with a value
   * equal to {@value}
   *
   */
  public boolean isNumericScalarConstantEqualTo(double value) {
    if(constantValue == null) {
      return false;
    }
    if(constantValue.length() != 1) {
      return false;
    }
    if(!(constantValue instanceof DoubleVector || constantValue instanceof IntVector)) {
      return false;
    }
    AtomicVector vector = (AtomicVector) constantValue;
    return vector.getElementAsDouble(0) == value;
  }

  /**
   *
   * @return true if this is an atomic vector of length 1
   */
  public boolean isScalar() {
    return TypeSet.isDefinitelyAtomic(typeSet) && length == 1;
  }

  public static class Builder {
    private ValueBounds bounds;

    public Builder() {
      bounds = new ValueBounds();
    }
    
    public Builder(ValueBounds bounds) {
      this.bounds = new ValueBounds(bounds);
    }
    
    public Builder setLength(int length) {
      bounds.length = length;
      return this;
    }
    
    public Builder setTypeSet(int typeSet) {
      bounds.typeSet = typeSet;
      return this;
    }

    public Builder setNA(int na) {
      bounds.na = na;
      return this;
    }

    public Builder setType(Class type) {
      return setTypeSet(TypeSet.of(type));
    }

    public Builder setEmptyAttributes() {
      bounds.attributes = Collections.emptyMap();
      bounds.attributesOpen = false;
      return this;
    }
    
    public void setAttributeBounds(Map<Symbol, SEXP> attributes) {
      bounds.attributes = attributes;
    }
    
    public void setAttributeSetOpen(boolean open) {
      bounds.attributesOpen = open;
    }

    public void setClosedAttributes(Map<Symbol, SEXP> attributes) {
      bounds.attributes = attributes;
      bounds.attributesOpen = false;
    }

    
    public void setAttribute(Symbol name, SEXP value) {
      if(bounds.attributes == null) {
        bounds.attributes = new HashMap<>();
      }
      bounds.attributes.put(name, value);
    }

    public void attributeCouldBePresent(Symbol attributeName) {
      setAttribute(attributeName, null);
    }
    
    public void setDimAttribute(AtomicVector value) {
      setAttribute(Symbols.DIM, value);
    }
    
    public ValueBounds build() {
      if(bounds.attributes == null) {
        bounds.attributes = Collections.emptyMap();
      }
      return bounds;
    }

  }
}
