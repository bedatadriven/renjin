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

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.invoke.codegen.WrapperRuntime;
import org.renjin.primitives.Identical;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.sexp.*;

import java.util.Iterator;
import java.util.Objects;

/**
 * Describes the bounds of known types for an expression.
 */
public class ValueBounds {


  /**
   * The values has no elements which are NAs
   */
  public static final int FLAG_NO_NA = 1;

  /**
   * The values are all positive integers or real numbers > 0
   */
  public static final int FLAG_POSITIVE = 1 << 1;

  /**
   * This value *definitely* has a non-zero length
   */
  public static final int LENGTH_NON_ZERO = 1 << 2;

  /**
   * This value *definitely* has a length of one
   */
  public static final int LENGTH_ONE = (1 << 3) | LENGTH_NON_ZERO;


  /**
   * This value *may* have a class attribute
   */
  public static final int MAYBE_CLASS = 1 << 4;

  /**
   * This value *may* have a "dim" attribute of length one or greater
   */
  public static final int MAYBE_DIM = 1 << 5;

  /**
   * This value *may* have a "dimnames" attribute
   */
  public static final int MAYBE_DIMNAMES = 1 << 6;

  /**
   * This value *may* have a "names" attribute
   */
  public static final int MAYBE_NAMES = 1 << 7;

  /**
   * This value *may* have an attribute *other* than class, dim, dimnames, or names
   */
  public static final int MAYBE_OTHER_ATTR = 1 << 8;

  /**
   * This value *may* have attributes
   */
  public static final int MAYBE_ATTRIBUTES = MAYBE_CLASS | MAYBE_DIM | MAYBE_DIMNAMES | MAYBE_NAMES | MAYBE_OTHER_ATTR;

  /**
   * This value *definitely* has a dim attribute of length 1
   */
  public static final int HAS_DIM1 = 1 << 9;

  /**
   * This value *definitely* has a dim attribute of length 2
   */
  public static final int HAS_DIM2 = 1 << 10;


  /**
   * This value *definitely* has a dim attribute of length 3 or greater.
   */
  public static final int HAS_DIM_MORE = 1 << 11;

  public static final int HAS_DIM = HAS_DIM1 | HAS_DIM2 | HAS_DIM_MORE;

  private static final int INTERSECT_MASK = FLAG_NO_NA | FLAG_POSITIVE | LENGTH_NON_ZERO | LENGTH_ONE | HAS_DIM;

  private static final int UNION_MASK = MAYBE_ATTRIBUTES;


  public static final ValueBounds UNBOUNDED = new ValueBounds.Builder()
      .setTypeSet(TypeSet.ANY_TYPE)
      .addFlags(MAYBE_ATTRIBUTES)
      .build();

  
  /**
   * The bit set of this value's possible types.
   */
  private int typeSet = TypeSet.ANY_TYPE;


  /**
   * Bit set of properties about this value that are known to be true
   */
  private int flags;

  /**
   * This value, if constant. {@code null} if unknown or known to vary.
   */
  private SEXP constantValue = null;


  /**
   * The value of the class attribute, if constant.
   */
  private AtomicVector classAttribute;


  private ValueBounds() {}
  
  private ValueBounds(ValueBounds toCopy) {
    this.typeSet = toCopy.typeSet;
    this.flags = toCopy.flags;
    this.constantValue = toCopy.constantValue;
  }

  /**
   * Constructs a {@code ValueBounds} for a constant {@code SEXP}. That is, a value that is
   * entirely known at compile time.
   */
  public static ValueBounds constantValue(SEXP value) {
    Builder bounds = ValueBounds.builder();
    bounds.addFlags(FLAG_NO_NA, hasAnyNAs(value));
    bounds.setLength(value.length());
    bounds.setTypeSet(TypeSet.of(value));
    bounds.setAttributes(value.getAttributes());
    bounds.bounds.constantValue = value;
    return bounds.build();
  }

  private static boolean hasAnyNAs(SEXP value) {
    if(value instanceof AtomicVector) {
      if(((AtomicVector) value).containsNA()) {
        return true;
      }
    }
    return false;
  }

  public ValueBounds of(Object value) {
    if(value instanceof SEXP) {
      return constantValue((SEXP)value);
    } else if(value instanceof Integer) {
      return constantValue(IntVector.valueOf((Integer) value));
    } else if(value instanceof Double) {
      return constantValue(DoubleVector.valueOf((Double) value));
    }
    throw new UnsupportedOperationException("value: " + value);
  }


  public boolean isConstant() {
    return constantValue != null;
  }
  
  public boolean isConstant(Symbol symbol) {
    return constantValue == symbol;
  }

  public boolean hasUnknownClassAttribute() {
    if (!isFlagSet(MAYBE_CLASS)) {
      return false;
    }
    return classAttribute == null;
  }


  public boolean isDimCountConstant() {
    return !isFlagSet(MAYBE_DIM) || isAnyFlagSet(ValueBounds.HAS_DIM);
  }

  public AtomicVector getConstantClassAttribute() {
    return classAttribute;
  }

  /**
   * @return a bit mask indicating which R types are included in this bounds.
   */
  public int getTypeSet() {
    return typeSet;
  }

  public boolean isFlagSet(int flag) {
    return (flags & flag) == flag;
  }

  public boolean isAnyFlagSet(int mask) {
    return (flags & mask) != 0;
  }

  public int getLength() {
    if(isFlagSet(LENGTH_ONE)) {
      return 1;
    } else {
      return -1;
    }
  }

  /**
   * @return a new {@code TypeBounds} that is the union of this bounds and the {@code other}
   */
  public ValueBounds union(ValueBounds other) {
    ValueBounds u = new ValueBounds();
    u.typeSet = this.typeSet | other.typeSet;
    u.flags = ((this.flags & UNION_MASK) | (other.flags & UNION_MASK)) |
              ((this.flags & INTERSECT_MASK) & (other.flags & INTERSECT_MASK));
    u.classAttribute = unionClasses(this.classAttribute, u.classAttribute);
    return u;
  }

  private static AtomicVector unionClasses(AtomicVector x, AtomicVector y) {
    if(classesEqual(x, y)) {
      return x;
    } else {
      return null;
    }
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


  @Override
  public String toString() {
    
    if(isConstant()) {
      return "[const " + formatConstant(constantValue) + "]";
    }
    
    if(typeSet == TypeSet.ANY_TYPE) {
      return "[*]";
    }
    
    StringBuilder s = new StringBuilder();
    s.append("[");
    s.append(TypeSet.toString(typeSet));

    if(isFlagSet(LENGTH_ONE)) {
      s.append(", len=1");
    } else if (isFlagSet(LENGTH_NON_ZERO)) {
      s.append(", len>0");
    } else {
      s.append(", len=*");
    }
    if(!isFlagSet(FLAG_NO_NA)) {
      s.append(", ?NA");
    }
    if(isFlagSet(FLAG_POSITIVE)) {
      s.append(", pos");
    }
    if(isFlagSet(HAS_DIM1)) {
      s.append(", dim=[*]");
    } else if(isFlagSet(HAS_DIM2)) {
      s.append(", dim=[*,*]");
    } else if(isFlagSet(HAS_DIM_MORE)) {
      s.append(", dim=[*,*,...]");
    } else if(isFlagSet(MAYBE_DIM)) {
      s.append(", dim?");
    }
    if (isFlagSet(MAYBE_NAMES)) {
      s.append(", names?");
    }
    if (isFlagSet(MAYBE_DIMNAMES)) {
      s.append(", dimnames?");
    }
    if (classAttribute != null) {
      s.append(", class=").append(classAttribute);
    } else if (isFlagSet(MAYBE_CLASS)) {
      s.append(", class?");
    }
    if (isFlagSet(MAYBE_OTHER_ATTR)) {
      s.append(", attr?");
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
    ValueBounds bounds = (ValueBounds) o;
    return
        typeSet == bounds.typeSet &&
        flags == bounds.flags &&
        Objects.equals(constantValue, bounds.constantValue) &&
        classesEqual(classAttribute, bounds.classAttribute);
  }

  private static boolean classesEqual(AtomicVector x, AtomicVector y) {
    if(x == null && y == null) {
      return true;

    } else if(x != null && y != null) {
      if (x.length() != y.length()) {
        return false;
      }
      for (int i = 0; i < x.length(); i++) {
        if (!Objects.equals(x.getElementAsString(i), y.getElementAsString(i))) {
          return false;
        }
      }
      return true;

    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeSet, flags);
  }

  public SEXP getConstantValue() {
    return constantValue;
  }

  public static boolean allConstantArguments(Iterable<ArgumentBounds> argumentTypes) {
    for (ArgumentBounds argumentType : argumentTypes) {
      if(!argumentType.getBounds().isConstant()) {
        return false;
      }
    }
    return true;
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
    bounds.flags = flags;
    bounds.typeSet = this.typeSet;
    bounds.classAttribute = this.classAttribute;
    return bounds;
  }

  public static ValueBounds.Builder builder() {
    return new Builder();
  }

  /**
   * Returns true if the given {@code sexp} falls within these {@code ValueBounds}.
   */
  public boolean test(SEXP sexp) {
    if(constantValue != null) {
      return Identical.identical(constantValue, sexp);
    }
    int sexpType = TypeSet.of(sexp);
    if((this.typeSet & sexpType) == 0) {
      return false;
    }
    if(isFlagSet(LENGTH_ONE)) {
      if(sexp.length() != 1) {
        return false;
      }
    } else if(isFlagSet(LENGTH_NON_ZERO)) {
      if(sexp.length() == 0) {
        return false;
      }
    }
    if(isFlagSet(FLAG_NO_NA) && sexp instanceof Vector) {
      Vector vector = (Vector) sexp;
      if(vector.anyNA()) {
        return false;
      }
    }

    AttributeMap attributes = sexp.getAttributes();
    if(hasNoAttributes() && attributes.isEmpty()) {
      return true;
    }
    for (Symbol attribute : attributes.names()) {
      if(attribute == Symbols.CLASS) {

      } else if (attribute == Symbols.NAMES) {
        if (!isFlagSet(MAYBE_NAMES)) {
          return false;
        }
      } else if (attribute == Symbols.DIMNAMES) {
        if (!isFlagSet(MAYBE_DIMNAMES)) {
          return false;
        }
      } else if (attribute == Symbols.DIM) {
        if (!isFlagSet(MAYBE_DIM)) {
          return false;
        }
      } else {
        if (!isFlagSet(MAYBE_OTHER_ATTR)) {
          return false;
        }
      }
    }

    return true;
  }

  public boolean isConstantFlagEqualTo(boolean value) {
    if(constantValue == null) {
      return false;
    }
    return WrapperRuntime.convertToBooleanPrimitive(constantValue) == value;
  }

  public int getFlags() {
    return flags;
  }

  public boolean hasNoAttributes() {
    return !isAnyFlagSet(MAYBE_ATTRIBUTES);
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
      if(length == 1) {
        addFlags(ValueBounds.LENGTH_ONE);
      } else if(length > 0) {
        addFlags(ValueBounds.LENGTH_NON_ZERO);
      }
      return this;
    }
    
    public Builder setTypeSet(int typeSet) {
      bounds.typeSet = typeSet;
      return this;
    }

    public Builder addFlags(int flag, boolean set) {
      if(set) {
        bounds.flags |= flag;
      } else {
        bounds.flags &= ~flag;
      }
      return this;
    }

    public Builder addFlags(int flag) {
      return addFlags(flag, true);
    }

    public Builder addFlagsFrom(ValueBounds source, int mask) {
      return addFlags(source.getFlags() & mask);
    }

    public Builder setType(Class type) {
      return setTypeSet(TypeSet.of(type));
    }

    public Builder setDimCount(int count) {
      if(count > 0) {
        bounds.flags |= MAYBE_DIM;
      }
      if(count == 1) {
        bounds.flags |= HAS_DIM1;
      } else if(count == 2) {
        bounds.flags |= HAS_DIM2;
      } else {
        bounds.flags |= HAS_DIM_MORE;
      }
      return this;
    }

    public Builder setClassAttribute(SEXP classVector) {
      if(classVector instanceof AtomicVector) {
        bounds.flags |= MAYBE_CLASS;
        bounds.classAttribute = (AtomicVector) classVector;
      }
      return this;
    }

    public void setAttributes(AttributeMap attributes) {
      for (Symbol symbol : attributes.names()) {
        if(symbol == Symbols.DIM) {
          setDimCount((short)attributes.getDim().length());

        } else if(symbol == Symbols.DIMNAMES) {
          addFlags(ValueBounds.MAYBE_DIMNAMES);

        } else if(symbol == Symbols.NAMES) {
          addFlags(ValueBounds.MAYBE_NAMES);

        } else if(symbol == Symbols.CLASS) {
          setClassAttribute(attributes.getClassVector());

        } else {
          addFlags(ValueBounds.MAYBE_OTHER_ATTR);
        }
      }
    }

    public ValueBounds build() {
      return bounds;
    }
  }
}
