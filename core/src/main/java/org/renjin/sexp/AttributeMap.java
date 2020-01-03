/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.sexp;

import org.renjin.eval.EvalException;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.repackaged.guava.base.Strings;

import java.util.*;
import java.util.function.BiConsumer;


/**
 *
 * Immutable map of a SEXP's attributes.
 *
 * <p>In R, any value can be associated with zero or more arbitrary
 * key/value pairs called attributes.</p>
 *
 * <p>There are many "special" attributes however, that
 * determine how the value is interpreted. The
 * The most commonly accessed attributes
 * are stored in this structure as direct pointers, others in a HashMap.
 */
public class AttributeMap {
  private StringVector classes = null;
  private SEXP names = null;
  private SEXP dim = null;
  private SEXP dimNames = null;
  private boolean s4 = false;

  private Symbol[] attributeNames;
  private SEXP[] attributeValues;


  public static final AttributeMap EMPTY = new AttributeMap().initEmpty();

  private AttributeMap() {
  }

  private AttributeMap initEmpty() {
    attributeNames = new Symbol[0];
    attributeValues = new SEXP[0];
    return this;
  }

  /**
   *
   * @return the <em>dim</em> attribute, or {@code Null.INSTANCE} if no
   * <em>dim</em> attribute is present.
   */
  public Vector getDim() {
    return dim == null ? Null.INSTANCE : (Vector) dim;
  }

  /**
   *
   * @return a list of the names of the attributes
   * present in the map.
   */
  public Iterable<Symbol> names() {
    return Arrays.asList(attributeNames);
  }

  public PairList asPairList() {
    PairList.Builder list = asPairListBuilder();
    return list.build();
  }

  public PairList.Builder asPairListBuilder() {
    PairList.Builder list = new PairList.Builder();
    addTo(list);
    return list;
  }

  public ListVector toVector() {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    addTo(list);
    return list.build();
  }

  private void addTo(ListBuilder list) {
    int length = attributeNames.length;
    for (int i = 0; i < length; i++) {
      list.add(attributeNames[i], attributeValues[i]);
    }
  }

  public void forEach(BiConsumer<Symbol, SEXP> consumer) {
    for (int i = 0; i < attributeNames.length; i++) {
      consumer.accept(attributeNames[i], attributeValues[i]);
    }
  }

  public Map<Symbol, SEXP> toMap() {
    Map<Symbol, SEXP> map = new HashMap<>();
    int length = attributeNames.length;
    for (int i = 0; i < length; i++) {
      map.put(attributeNames[i], attributeValues[i]);
    }
    return map;
  }

  public SEXP get(String what) {
    return get(Symbol.get(what));
  }

  public boolean has(Symbol name) {
    return get(name) != Null.INSTANCE;
  }

  public SEXP get(Symbol name) {
    int length = attributeNames.length;
    for (int i = 0; i < length; i++) {
      if(attributeNames[i] == name) {
        return attributeValues[i];
      }
    }
    return Null.INSTANCE;
  }

  public Builder copy() {
    return new Builder(this);
  }

  public boolean hasAnyBesidesName() {
    int length = attributeNames.length;
    if(length == 0) {
      return false;
    }
    if(length > 1) {
      return true;
    }
    return attributeNames[0] != Symbols.NAMES;
  }

  public boolean hasAnyBesidesS4Flag() {
    return attributeNames.length > 0;
  }

  private boolean hasDim() {
    return dim != null;
  }


  public Iterable<PairList.Node> nodes() {
    return asPairList().nodes();
  }

  public int[] getDimArray() {
    if(dim == null) {
      return new int[0];
    }
    return ((IntVector) dim).toIntArray();
  }

  /**
   * @throws ClassCastException if the dimnames is non-null and not
   * a vector.
   */
  public Vector getDimNames() {
    if(dimNames == null) {
      return Null.INSTANCE;
    } else {
      return (Vector) dimNames;
    }
  }

  public AtomicVector getDimNames(int i) {
    if(dimNames == null) {
      return Null.INSTANCE;
    }
    return (AtomicVector) dimNames.getElementAsSEXP(0);
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean hasNames() {
    return names != null;
  }

  public StringVector getNames() {
    return (StringVector) names;
  }

  public boolean isS4() {
    return this.s4;
  }

  public boolean isEmpty() {
    return this == EMPTY;
  }

  /**
   *
   * @return {@code true} if this map contains a <em>class</em> attribute.
   */
  public boolean hasClass() {
    return classes != null;
  }

  public AtomicVector getClassVector() {
    return classes == null ? Null.INSTANCE : classes;
  }

  public AtomicVector getNamesOrNull() {
    return names == null ? Null.INSTANCE : (AtomicVector) names;
  }
  
  public ListVector getDimNamesOrEmpty() {
    return dimNames == null ? ListVector.EMPTY : (ListVector) dimNames;
  }

  /**
   *
   * @return a new {@code AttributeMap}, containing only the
   * this {@code AttributeMap}'s {@code dim}, {@code names}, and {@code dimnames} attributes.
   */
  public AttributeMap copyStructural() {
    int newLength = 0;
    if(dim != null) {
      newLength++;
    }
    if(dimNames != null) {
      newLength++;
    }
    if(names != null) {
      newLength++;
    }
    if(newLength == 0) {
      return AttributeMap.EMPTY;
    } else {
      AttributeMap newMap = new AttributeMap();
      newMap.dim = dim;
      newMap.dimNames = dimNames;
      newMap.names = names;
      newMap.attributeNames = new Symbol[newLength];
      newMap.attributeValues = new SEXP[newLength];
      int j = 0;
      for (int i = 0; i < attributeNames.length; i++) {
        Symbol attributeName = attributeNames[i];
        if(attributeName == Symbols.DIM || attributeName == Symbols.DIMNAMES || attributeName == Symbols.NAMES) {
          newMap.attributeNames[j] = attributeNames[i];
          newMap.attributeValues[j] = attributeValues[i];
          j++;
        }
      }
      return newMap;
    }
  }


  /**
   * Combines the attributes from vectors <em>x</em> and <em>y</em> according
   * to the R language rules:
   * <ul>
   *   <li>If <em>x</em> is longer, only <em>x</em>'s attributes are copied</li>
   *   <li>If <em>y</em> is longer, only <em>y</em>'s attributes are copied</li>
   *   <li>If <em>x</em> and <em>y</em> are the same length, attributes from both <em>x</em>
   *   and <em>y</em> are copied, with those of <em>x</em> taking precedence.</li>
   * </ul>
   *
   */
  public static AttributeMap combineAttributes(Vector x, Vector y) {
    if(x.length() > y.length()) {
      return x.getAttributes();
    } else if(y.length() > x.length()) {
      return y.getAttributes();
    } else {
      Builder builder = new Builder(x.getAttributes());
      builder.combineFrom(y.getAttributes());
      
      return builder.build();
    }
  }
  
  /**
   * Combines the <em>dim</em>, <em>names</em>, and <em>dimnames</em> attributes from
   * vectors <em>x</em> and <em>y</em> according
   * to the R language rules:
   * <ul>
   *   <li>If <em>x</em> is longer, only <em>x</em>'s attributes are copied</li>
   *   <li>If <em>y</em> is longer, only <em>y</em>'s attributes are copied</li>
   *   <li>If <em>x</em> and <em>y</em> are the same length, attributes from both <em>x</em>
   *   and <em>y</em> are copied, with those of <em>x</em> taking precedence.</li>
   * </ul>
   *
   */
  public static AttributeMap combineStructuralAttributes(Vector x, Vector y) {
    if(x.length() > y.length()) {
      return x.getAttributes().copyStructural();
    } else if(y.length() > x.length()) {
      return y.getAttributes().copyStructural();
    } else {
      Builder builder = new Builder();
      builder.combineStructuralFrom(x.getAttributes());
      builder.combineStructuralFrom(y.getAttributes());
      return builder.build();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributeMap that = (AttributeMap) o;
    return s4 == that.s4 &&
        Arrays.equals(attributeNames, that.attributeNames) &&
        Arrays.equals(attributeValues, that.attributeValues);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(s4);
    result = 31 * result + Arrays.hashCode(attributeNames);
    result = 31 * result + Arrays.hashCode(attributeValues);
    return result;
  }

  public static Builder newBuilder() {
    return new Builder();
  }


  public static class Builder {
    private boolean s4 = false;
    private StringVector classes = null;
    private SEXP names = null;
    private SEXP dim = null;
    private SEXP dimNames = null;

    private List<Symbol> attributeNames;
    private List<SEXP> attributeValues;

    public Builder() {
      this.attributeNames = new ArrayList<>();
      this.attributeValues = new ArrayList<>();
    }

    private Builder(AttributeMap attributes) {
      this.s4 = attributes.s4;
      this.classes = attributes.classes;
      this.names = attributes.names;
      this.dim = attributes.dim;
      this.dimNames = attributes.dimNames;

      int length = attributes.attributeNames.length;
      this.attributeNames = new ArrayList<>(length);
      this.attributeValues = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        attributeNames.add(attributes.attributeNames[i]);
        attributeValues.add(attributes.attributeValues[i]);
      }
    }

    public Builder setS4(boolean flag) {
      this.s4 = flag;
      return this;
    }


    /**
     * Sets the {@code dim} attribute.
     * @param value the new value of the {@code dim} attribute
     * @throws EvalException if {@code value} is not {@code Null.INSTANCE} or cannot be converted to an
     * {@code IntVector}
     */
    public Builder setDim(SEXP value) {
      if(value instanceof Null) {
        this.dim = null;
        removeList(Symbols.DIM);

      } else {
        if (!(value instanceof Vector)) {
          throw new EvalException("Invalid dim attribute of type '%s'", value.getTypeName());
        }
        if (value instanceof IntVector) {
          dim = value;
        } else {
          IntArrayVector.Builder dimVector = new IntArrayVector.Builder(0, value.length());
          for (int i = 0; i < value.length(); i++) {
            dimVector.add(((AtomicVector) value).getElementAsInt(i));
          }
          dim = dimVector.build();
        }
        updateList(Symbols.DIM, dim);
      }
      return this;
    }

    private void updateList(Symbol name, SEXP value) {
      int sz = attributeNames.size();
      for (int i = 0; i < sz; i++) {
        if(attributeNames.get(i) == name) {
          attributeValues.set(i, value);
          return;
        }
      }
      attributeNames.add(name);
      attributeValues.add(value);
    }

    private void updateListIfNotPresent(Symbol name, SEXP value) {
      int sz = attributeNames.size();
      for (int i = 0; i < sz; i++) {
        if(attributeNames.get(i) == name) {
          return;
        }
      }
      attributeNames.add(name);
      attributeValues.add(value);
    }

    private void removeList(Symbol name) {
      int sz = attributeNames.size();
      for (int i = 0; i < sz; i++) {
        if(attributeNames.get(i) == name) {
          attributeNames.remove(i);
          attributeValues.remove(i);
          return;
        }
      }
    }

    /**
     * Sets the {@code dim} attribute
     */
    public Builder setDim(IntVector dim) {
      assert dim != null;
      this.dim = dim;
      updateList(Symbols.DIM, dim);
      return this;
    }

    public Builder setDim(int rows, int cols) {
      return setDim(new IntArrayVector(rows, cols));
    }

    public Builder setNames(StringVector names) {
      assert names != null;
      this.names = names;
      updateList(Symbols.NAMES, names);
      return this;
    }

    public Builder setNames(SEXP names) {
      if(names == Null.INSTANCE) {
        this.names = null;
        removeList(Symbols.NAMES);
        return this;
      } else if(names instanceof StringVector) {
        return setNames((StringVector) names);
      } else if(names instanceof Vector) {
        return setNames(new ConvertingStringVector((Vector) names));
      } else {
        throw new EvalException("'names' vector must be a character");
      }
    }

    public Builder setClass(StringVector className) {
      this.classes = className;
      updateList(Symbols.CLASS, classes);
      return this;
    }

    public Builder setClass(String... classNames) {
      return setClass(new StringArrayVector(classNames));
    }

    /**
     * Validates and sets a {@code class} attribute value.
     */
    public Builder setClass(SEXP value) {
      if(value.length() == 0) {
        return remove(Symbols.CLASS);
      } else {
        return setClass(toClassVector(value));
      }
    }
    
    public Builder setDimNames(SEXP value) {
      assert value != null;

      if(value == Null.INSTANCE) {
        return remove(Symbols.DIMNAMES);
      } 
    
      if (!(value instanceof ListVector)) {
        throw new EvalException("'dimnames' must be a list");
      }
      this.dimNames = value;
      updateList(Symbols.DIMNAMES, value);

      // Arrays cannot have both dimnames and names
      if(this.dim != null && this.dim.length() == 1) {
        this.names = null;
        removeList(Symbols.NAMES);
      }

      return this;
    }

    /**
     * Sets a 1-dimensional {@code DIMNAMES} attribute 
     */
    public Builder setArrayNames(Vector names) {
      if(names == Null.INSTANCE) {
        remove(Symbols.DIMNAMES);
      } else if(names instanceof StringVector) {
        setDimNames(new ListVector(names));
      } else {
        throw new IllegalArgumentException("" + names);
      }
      return this;
    }

    public Builder set(String name, SEXP value) {
      return set(Symbol.get(name), value);
    }

    public Builder set(Symbol name, SEXP value) {

      if(value == Null.INSTANCE) {
        return remove(name);
      } else {

        if(name == Symbols.CLASS) {
          setClass(value);
          
        } else if(name == Symbols.NAMES) {
          setNames(value);

        } else if(name == Symbols.DIM) {
          setDim(value);

        } else if(name == Symbols.DIMNAMES) {
          setDimNames(value);

        } else {
          if(name == Symbols.ROW_NAMES) {
            updateList(name, validateRowNames(value));
          } else {
            updateList(name, value);
          }
        }
      }
      return this;
    }


    public Builder setSlot(Symbol name, SEXP value) {
      if(value == Null.INSTANCE) {
        return remove(name);
      } else if(name == Symbols.CLASS) {
        // Even for S4 objects, the class attribute MUST be a string vector
        setClass(value);
      } else {
        // For other attributes, we do not apply the normal constraints
        // as S4 classes are free to define the shape of these slots.
        if(name == Symbols.NAMES) {
          this.names = value;

        } else if(name == Symbols.DIM) {
          this.dim = value;

        } else if(name == Symbols.DIMNAMES) {
          this.dimNames = value;
        }
        updateList(name, value);
      }
      return this;
    }

    /**
     * Validates the {@code row.names} attribute
     *
     * @param rowNames the {@code row.names} vector to validate
     * @return the given {@code rowNames} vector, possibly in compact form. 
     * @throws EvalException if {@code rowNames} is not a {@link StringVector} or a {@link IntArrayVector}
     */
    private Vector validateRowNames(SEXP rowNames) {

      // GNU R used a special "compact format" for row.names that are an integer sequence 1..n
      // in the format c(NA, -n). Renjin does not need/want this, so expand it to something useful

      if(RowNamesVector.isOldCompactForm(rowNames)) {
        return RowNamesVector.fromOldCompactForm(rowNames);

      } else if(rowNames instanceof Vector) {
        return (Vector)rowNames;
      }
      throw new EvalException("row names must be 'character' or 'integer', not '%s'", rowNames.getTypeName());
    }



    public Builder remove(Symbol name) {
      if(name == Symbols.CLASS) {
        this.classes = null;
      } else if(name == Symbols.NAMES) {
        this.names = null;
      } else if(name == Symbols.DIM) {
        this.dim = null;
      } else if(name == Symbols.DIMNAMES) {
        this.dimNames = null;
      }
      removeList(name);
      return this;
    }

    public Builder removeDim() {
      dim = null;
      dimNames = null;
      removeList(Symbols.DIM);
      removeList(Symbols.DIMNAMES);
      return this;
    }

    public Builder removeDimnames() {
      dimNames = null;
      removeList(Symbols.DIMNAMES);
      return this;
    }

    public SEXP get(String what) {
      return get(Symbol.get(what));
    }

    public SEXP get(Symbol name) {
      int length = attributeNames.size();
      for (int i = 0; i < length; i++) {
        if(attributeNames.get(i) == name) {
          return attributeValues.get(i);
        }
      }
      return Null.INSTANCE;
    }

    /**
     * Combines the attributes from {@code attributes} with this builder, enforcing
     * a few consistency rules.
     *
     * <ul>
     *   <li>{@code names} are only copied if {@code dim} is not present</li>
     *   <li>If this already has a dimension, then combining with a different {@code dim} throws an EvalException</li>
     * </ul>
     */
    public Builder combineFrom(AttributeMap other) {
      return combineFrom(other, true);
    }

    /**
     * Combines the {@code name}, {@code dim} and {@code dimnames} attributes from
     * {@code attributes} with this builder, enforcing
     * a few consistency rules.
     *
     * <ul>
     *   <li>{@code names} are only copied if {@code dim} is not present</li>
     *   <li>If this already has a dimension, then combining with a different {@code dim} throws an EvalException</li>
     * </ul>
     */
    public Builder combineStructuralFrom(AttributeMap other) {
      if(attributeNames.isEmpty()) {
        // Fast path
        this.dim = other.dim;
        this.names = other.names;
        this.dimNames = other.dimNames;

        if(this.dim != null) {
          updateList(Symbols.DIM, dim);
        }
        if(this.names != null) {
          updateList(Symbols.NAMES, names);
        }
        if(this.dimNames != null) {
          updateList(Symbols.DIMNAMES, dimNames);
        }

      } else {
        combineFrom(other, false);
      }
      return this;
    }

    private Builder combineFrom(AttributeMap other, boolean all) {
      if(other == EMPTY) {
        return this;
      }

      int otherLen = other.attributeNames.length;
      for (int i = 0; i < otherLen; i++) {
        Symbol otherName = other.attributeNames[i];
        if(otherName == Symbols.NAMES) {
          if(this.names == null && this.dim == null) {
            this.names = other.attributeValues[i];
            updateList(Symbols.NAMES, this.names);
          }
        } else if(otherName == Symbols.DIM) {
          if(this.dim == null) {
            this.dim = other.attributeValues[i];
            updateList(Symbols.DIM, this.dim);
            if(this.names != null) {
              this.names = null;
              removeList(Symbols.NAMES);
            }
          } else {
            if(!conforming((IntVector) this.dim, (IntVector)other.dim)) {
              throw new EvalException("non-conformable arrays");
            }
          }
        } else if(otherName == Symbols.DIMNAMES) {
          if(this.dimNames == null) {
            this.dimNames = other.dimNames;
            updateList(Symbols.DIMNAMES, this.dimNames);
          }
        } else if(all) {
          if(otherName == Symbols.CLASS) {
            if(this.classes == null) {
              this.classes = other.classes;
              updateList(Symbols.CLASS, other.classes);
            }
          } else {
            updateListIfNotPresent(otherName, other.attributeValues[i]);
          }
        }
      }
      return this;
    }

    private boolean conforming(IntVector dim1, IntVector dim2) {
      if(dim1.length() != dim2.length()) {
        return false;
      }
      for (int i = 0; i < dim1.length(); i++) {
        if(dim1.getElementAsInt(i) != dim2.getElementAsInt(i)) {
          return false;
        }
      }
      return true;
    }

    /**

     * Copies all non-null attributes from {@code attributes} to this {@code Builder}
     */
    public Builder addAllFrom(AttributeMap attributes) {

      this.s4 = attributes.s4;

      int length = attributes.attributeNames.length;
      for (int i = 0; i < length; i++) {
        Symbol sourceName = attributes.attributeNames[i];
        SEXP sourceValue = attributes.attributeValues[i];
        if(sourceName == Symbols.NAMES) {
          this.names = sourceValue;
        } else if(sourceName == Symbols.DIM) {
          this.dim = sourceValue;
        } else if(sourceName == Symbols.DIMNAMES) {
          this.dimNames = sourceValue;
        } else if(sourceName == Symbols.CLASS) {
          this.classes = (StringVector) sourceValue;
        }
        updateList(sourceName, sourceValue);
      }
      return this;
    }

    public AttributeMap build() {
      if(isEmpty()) {
        return AttributeMap.EMPTY;
      }

      AttributeMap attributes = new AttributeMap();
      attributes.classes = classes;
      attributes.dim = dim;
      attributes.dimNames = dimNames;
      attributes.s4 = this.s4;
      attributes.names = names;
      attributes.attributeNames = attributeNames.toArray(new Symbol[0]);
      attributes.attributeValues = attributeValues.toArray(new SEXP[0]);
      return attributes;
    }

    private boolean isEmpty() {
      return !s4 && attributeNames.isEmpty();
    }

    public AttributeMap validateAndBuildFor(SEXP vector) {
      return validateAndBuildForVectorOfLength(vector.length());
    }
    
    /**
     * Builds the new {@code AttributeMap}, validating the attributes for 
     * a SEXP of the given {@code length}
     * @param length the length of the object 
     * @return a valid {@code AttributeMap}
     */
    public AttributeMap validateAndBuildForVectorOfLength(int length) {

      if(isEmpty()) {
        return AttributeMap.EMPTY;
      }

      validateDim(length);

      Vector validatedDimNames = validateDimNames();
      if(validatedDimNames != null) {
        this.dimNames = validatedDimNames;
        updateList(Symbols.DIMNAMES, validatedDimNames);
      }

      StringVector validatedNames = validateNames(length);
      if(validatedNames != null) {
        this.names = validatedNames;
        updateList(Symbols.NAMES, validatedNames);
      }
      return build();
    }

    private void validateDim(int vectorLength) {

      if (dim == null) {
        return;
      }

      int prod = 1;
      for (int i = 0; i != dim.length(); ++i) {
        int dimLength = ((IntVector) dim).getElementAsInt(i);
        if (dimLength < 0) {
          throw new EvalException("the dims contain negative values");
        }
        prod *= dimLength;
      }

      if (prod != vectorLength) {
        throw new EvalException("dims [product %d] do not match the length of object [%d]", prod, vectorLength);
      }

    }

    private Vector validateDimNames() {

      if(dimNames == null) {
        return null;
      }
      
      if(dimNames.length() == 0) {
        return Null.INSTANCE;
      }

      if(dim == null) {
        throw new EvalException("'dimnames' applied to non-array");
      }

      if(dimNames.length() > dim.length()) {
        throw new EvalException("length of 'dimnames' [%d] must match that of 'dims' [%d]",
            dimNames.length(), dim.length());
      }

      // Build a clean list with converted/validated names vectors
      ListVector.Builder builder = new ListVector.Builder();
      builder.setAttribute(Symbols.NAMES, dimNames.getNames());
      for (int i = 0; i < dim.length(); i++) {
        if(i < dimNames.length()) {
          builder.add(validateNames(i, dimNames.getElementAsSEXP(i)));
        } else {
          builder.add(Null.INSTANCE);
        }
      }
      
      return builder.build();
    }
    
    private StringVector validateNames(int vectorLength) {
      
      if(names == null) {
        return null;
      }
      
      if(this.names.length() < vectorLength) {
        StringVector.Builder extendedNames = ((StringVector) names).newCopyBuilder();
        while(extendedNames.length() < vectorLength) {
          extendedNames.addNA();
        }
        return extendedNames.build();
      }
      if(this.names.length() > vectorLength) {
        throw new EvalException("'names' attribute [%d] must be the same length as the vector [%d]", 
            names.length(), vectorLength);
      }

      return null;
      
    }

    private Vector validateNames(int dimIndex, SEXP names) {
      if(!(names instanceof Vector)) {
        throw new EvalException("invalid type (%s) for 'dimnames' (must be a vector)", names.getTypeName());
      }
      // Treat empty atomic vectors, like character(0) as NULL
      if(names.length() == 0) {
        return Null.INSTANCE;
      }
      if (!(dim instanceof IntVector)) {
        throw new EvalException("invalid type (%s) for 'dim' (must be integer vector)", dim.getTypeName());
      }
      int dimLength = ((IntVector) dim).getElementAsInt(dimIndex);
      if(names.length() != dimLength) {
        throw new EvalException("for dimension [%d], length of 'dimnames' [%d] not equal to array extent [%d]", 
            dimIndex, names.length(), dimLength);
      }
      return toNameVector(names);
    }

    private StringVector toNameVector(SEXP sexp) {
      if(sexp instanceof StringVector) {
        return (StringVector)sexp.setAttributes(AttributeMap.EMPTY);
      } else if(sexp instanceof Vector) {
        return StringArrayVector.fromVector((Vector) sexp);
      } else {
        throw new EvalException("Cannot coerce '%s' to character", sexp.getTypeName());
      }
    }

    private StringVector toClassVector(SEXP sexp) {
      if(sexp instanceof StringVector) {
        return (StringVector)sexp;
      } else if(sexp instanceof Vector) {
        return StringArrayVector.fromVector((Vector) sexp);
      } else {
        throw new EvalException("Cannot coerce '%s' to character", sexp.getTypeName());
      }
    }

  }

  public static AttributeMap fromListVector(ListVector attributes) {

    Builder builder = new Builder();
    for(int i=0;i!=attributes.length();++i) {
      String attributeName = attributes.getName(i);
      if(Strings.isNullOrEmpty(attributeName)) {
        throw new EvalException("Attributes must be named");
      }
      SEXP attributeValue = attributes.getElementAsSEXP(i);
      if(attributeValue != Null.INSTANCE) {
        builder.set(Symbol.get(attributeName), attributeValue);
      }
    }
    return builder.build();
  }

  public static AttributeMap fromPairList(PairList list) {
    if(list == Null.INSTANCE) {
      return AttributeMap.EMPTY;
    } else {

      Builder attributes = new Builder();
      for(PairList.Node node : list.nodes()) {
        attributes.set(node.getTag(), node.getValue());
      }
      return attributes.build();
    }
  }

  public static AttributeMap dim(int row, int col) {
    AttributeMap map = new AttributeMap();
    map.dim = new IntArrayVector(row, col);
    map.attributeNames = new Symbol[] { Symbols.DIM };
    map.attributeValues = new SEXP[] { map.dim };
    return map;
  }

  public String getString(Symbol name) {
    SEXP value = get(name);
    if(value == Null.INSTANCE) {
      return null;
    }
    if(value instanceof StringVector) {
      if(value.length() == 1) {
        return ((StringVector) value).getElementAsString(0);
      } else if(value.length() == 0) {
        return null;
      }
    }
    throw new EvalException("Expected character(1) value for attribute %s", name.getPrintName());
  }

  public String getPackage() {
    return getString(Symbols.PACKAGE);
  }
}
