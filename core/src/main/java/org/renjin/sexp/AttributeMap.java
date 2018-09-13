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
package org.renjin.sexp;

import org.renjin.eval.EvalException;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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

  private HashMap<Symbol, SEXP> map;


  public static final AttributeMap EMPTY = new AttributeMap();

  private AttributeMap() {
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
    List<Symbol> list = Lists.newArrayList();
    if(classes != null) {
      list.add(Symbols.CLASS);
    }
    if(names != null) {
      list.add(Symbols.NAMES);
    }
    if(dim != null) {
      list.add(Symbols.DIM);
    }
    if(dimNames != null) {
      list.add(Symbols.DIMNAMES);
    }
    if(map != null) {
      list.addAll(map.keySet());
    }
    return list;
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
    if(classes != null) {
      list.add(Symbols.CLASS, classes);
    }
    if(names != null) {
      list.add(Symbols.NAMES, names);
    }
    if(dim != null) {
      list.add(Symbols.DIM, dim);
    }
    if(dimNames != null) {
      list.add(Symbols.DIMNAMES, dimNames);
    }
    if(map != null) {
      for(Map.Entry<Symbol, SEXP> entry : map.entrySet()) {
        list.add(entry.getKey(), entry.getValue());
      }
    }
  }

  public Map<Symbol, SEXP> toMap() {
    Map<Symbol, SEXP> map = new HashMap<>();
    if(classes != null) {
      map.put(Symbols.CLASS, classes);
    }
    if(names != null) {
      map.put(Symbols.NAMES, names);
    }
    if(dim != null) {
      map.put(Symbols.DIM, dim);
    }
    if(dimNames != null) {
      map.put(Symbols.DIMNAMES, dimNames);
    }
    if(this.map != null) {
      map.putAll(this.map);
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
    if(name == Symbols.CLASS) {
      return classes != null ? classes : Null.INSTANCE;

    } else if(name == Symbols.DIM) {
      return dim != null ? dim : Null.INSTANCE;

    } else if(name == Symbols.NAMES) {
      return names != null ? names : Null.INSTANCE;

    } else if(name == Symbols.DIMNAMES) {
      return dimNames != null ? dimNames : Null.INSTANCE;

    } else if(map != null) {
      SEXP value = map.get(name);
      if(value != null) {
        return value;
      }
    }
    return Null.INSTANCE;
  }

  public Builder copy() {
    return new Builder(this);
  }

  public BuilderS4 copyS4() {
    return new BuilderS4(this);
  }

  public boolean hasAnyBesidesName() {
    return map != null || classes != null|| dim != null || dimNames != null;
  }

  public boolean hasAnyBesidesS4Flag() {
    return map != null || classes != null|| dim != null || dimNames != null || names != null;
  }

  public boolean hasDim() {
    return dim != null;
  }

  public boolean hasDimNames() {
    return dimNames != null;
  }

  public boolean hasOther() {
    return map != null;
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
   * @return
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
   * @return  a copy of this {@code AttributeMap} containing
   * this {@code AttributeMap}'s {@code names} attribute.
   */
  public AttributeMap copyNames() {
    if(names == null ) {
      return AttributeMap.EMPTY;
    } else {
      AttributeMap attributes = new AttributeMap();
      attributes.names = names;
      return attributes;
    }
  }

  /**
   *
   * @return a new {@code AttributeMap}, containing only the
   * this {@code AttributeMap}'s {@code dim}, {@code names}, and {@code dimnames} attributes.
   */
  public AttributeMap copyStructural() {
    if(classes == null && map == null) {
      return this;
    } else if(dim != null || names != null || (map != null && map.containsKey(Symbols.DIMNAMES))) {
      AttributeMap copy = new AttributeMap();
      copy.dim = this.dim;
      copy.names = this.names;
      if(this.map != null) {
        SEXP dimnames = map.get(Symbols.DIMNAMES);
        if(dimnames != null) {
          copy.map = Maps.newHashMap();
          copy.map.put(Symbols.DIMNAMES, dimnames);
        }
      }
      return copy;
    } else {
      return AttributeMap.EMPTY;
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AttributeMap that = (AttributeMap) o;

    if (classes != null ? !classes.equals(that.classes) : that.classes != null) {
      return false;
    }
    if (names != null ? !names.equals(that.names) : that.names != null) {
      return false;
    }
    if (dim != null ? !dim.equals(that.dim) : that.dim != null) {
      return false;
    }
    if (dimNames != null ? !dimNames.equals(that.dimNames) : that.dimNames != null) {
      return false;
    }
    if (this.s4 != that.s4) {
      return false;
    }
    return Objects.equals(map, that.map);

  }

  @Override
  public int hashCode() {
    int result = classes != null ? classes.hashCode() : 0;
    result = 31 * result + (names != null ? names.hashCode() : 0);
    result = 31 * result + (dim != null ? dim.hashCode() : 0);
    result = 31 * result + (dimNames != null ? dimNames.hashCode() : 0);
    result = 31 * result + (map != null ? map.hashCode() : 0);
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

    private HashMap<Symbol, SEXP> map;

    private boolean empty = true;

    public Builder() {
    }

    private Builder(AttributeMap attributes) {
      this.s4 = attributes.s4;
      this.classes = attributes.classes;
      this.names = attributes.names;
      this.dim = attributes.dim;
      this.dimNames = attributes.dimNames;
      if(attributes.map != null) {
        this.map = new HashMap<>(attributes.map);
      }
      updateEmptyFlag();
    }

    public Builder setS4(boolean flag) {
      this.s4 = flag;
      updateEmptyFlag();
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
        updateEmptyFlag();
      } else {
        if (!(value instanceof Vector)) {
          throw new EvalException("Invalid dim attribute of type '%s'", value.getTypeName());
        }
        if (value instanceof IntVector) {
          dim = (IntVector) value;
        } else {
          IntArrayVector.Builder dimVector = new IntArrayVector.Builder(0, value.length());
          for (int i = 0; i < value.length(); i++) {
            dimVector.add(((AtomicVector) value).getElementAsInt(i));
          }
          dim = dimVector.build();
        }
        this.empty = false;
      }
      return this;
    }

    /**
     * Sets the {@code dim} attribute
     */
    public Builder setDim(IntVector dim) {
      assert dim != null;
      this.dim = dim;
      this.empty = false;
      return this;
    }

    public Builder setDim(int rows, int cols) {
      this.dim = new IntArrayVector(rows, cols);
      this.empty = false;
      return this;
    }

    public Builder setNames(StringVector names) {
      assert names != null;
      this.names = names;
      this.empty = false;
      return this;
    }

    public Builder setNames(SEXP names) {
      if(names == Null.INSTANCE) {
        remove(Symbols.NAMES);
      } else {
        if(!(names instanceof StringVector)) {
          if(names instanceof Vector) {
            names = new ConvertingStringVector((Vector) names);
          } else {
            throw new EvalException("'names' vector must be a character");
          }
        }
        this.names = (StringVector) names;
        this.empty = false;
      }
      return this;
    }

    public Builder setClass(String... classNames) {
      this.classes = new StringArrayVector(classNames);
      this.empty = false;
      return this;
    }

    /**
     * Validates a {@code class} attribute value
     *
     * @param value the proposed {@code class} attribute
     * @return the {@code classNames} vector, coerced to {@link StringVector} if not null.
     */
    public Builder setClass(SEXP value) {
      if(value.length() == 0) {
        return remove(Symbols.CLASS);
      }
      this.classes = toClassVector(value);
      this.empty = false;
      return this;
    }
    
    public Builder setDimNames(SEXP value) {
      assert value != null;

      if(value == Null.INSTANCE) {
        return remove(Symbols.DIMNAMES);
      } 
    
      if (!(value instanceof ListVector)) {
        throw new EvalException("'dimnames' must be a list");
      }
      this.dimNames = (ListVector) value;
      this.empty = false;

      // Arrays cannot have both dimnames and names
      if(this.dim != null && this.dim.length() == 1) {
        this.names = null;
      }

      return this;
    }

    /**
     * Sets a 1-dimensional {@code DIMNAMES} attribute 
     * @param names
     * @return
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
          this.empty = false;
          if(map == null) {
            map = Maps.newHashMap();
          }
          if(name == Symbols.ROW_NAMES) {
            map.put(name, validateRowNames(value));
          } else {
            map.put(name, value);
          }
        }
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
      } else if(map != null) {
        map.remove(name);
      }
      updateEmptyFlag();
      return this;
    }

    private void updateEmptyFlag() {
      this.empty = (!s4 && classes == null && dim == null && dimNames == null && names == null &&
          (map == null || map.isEmpty()));
    }

    public Builder removeDim() {
      dim = null;
      dimNames = null;
      updateEmptyFlag();
      return this;
    }


    public Builder removeDimnames() {
      dimNames = null;
      updateEmptyFlag();
      return this;
    }

    public SEXP get(String what) {
      return get(Symbol.get(what));
    }

    public SEXP get(Symbol name) {
      if(name == Symbols.CLASS) {
        return classes;
      } else if(name == Symbols.NAMES) {
        return names;
      } else if(name == Symbols.DIM) {
        return dim;
      } else if(name == Symbols.DIMNAMES) {
        return dimNames;
      } else if(map != null && map.containsKey(name)) {
        return map.get(name);
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
      if(empty) {
        // Fast path
        this.dim = other.dim;
        this.names = other.names;
        this.dimNames = other.dimNames;
        if(this.dim != null || this.names != null || this.dimNames != null) {
          empty = false;
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
      
      if(other.names != null) {
        if(this.names == null && this.dim == null) {
          this.names = other.names;
        }
      }
      if(other.dim != null) {
        if(this.dim == null) {
          this.dim = other.dim;
          this.names = null;
          
        } else {
          if(!conforming((IntVector) this.dim, (IntVector) other.dim)) {
            throw new EvalException("non-conformable arrays");
          }
        }
        if(other.dimNames != null) {
          if(this.dimNames == null) {
            this.dimNames = other.dimNames;
          }
        }
      }
      if(all) {
        if (other.classes != null) {
          if (this.classes == null) {
            this.classes = other.classes;
          }
        }
        if (other.map != null) {
          if (this.map == null) {
            this.map = new HashMap<>(other.map);
          } else {
            for (Map.Entry<Symbol, SEXP> entry : other.map.entrySet()) {
              if (!this.map.containsKey(entry.getKey())) {
                this.map.put(entry.getKey(), entry.getValue());
              }
            }
          }
        }
      }
      updateEmptyFlag();
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
     * @param attributes
     */
    public Builder addAllFrom(AttributeMap attributes) {
      if(attributes.classes != null) {
        this.classes = attributes.classes;
        this.empty = false;
      }
      if(attributes.names != null) {
        this.names = attributes.names;
        this.empty = false;
      }
      if(attributes.dim != null) {
        this.dim = attributes.dim;
        this.empty = false;
      }
      if(attributes.dimNames != null) {
        this.dimNames = attributes.dimNames;
        this.empty = false;
      }
      if(attributes.s4) {
        this.s4 = attributes.s4;
        this.empty = false;
      }
      if(attributes.map != null) {
        for(Map.Entry<Symbol, SEXP> entry : attributes.map.entrySet()) {
          if(this.map == null) {
            this.map = new HashMap<>();
          }
          this.map.put(entry.getKey(), entry.getValue());
          this.empty = false;
        }
      }
      return this;
    }

    public Builder addIfNotNull(AttributeMap source, Symbol symbol) {
      SEXP value = source.get(symbol);
      if(value != Null.INSTANCE) {
        set(symbol, value);
      }
      return this;
    }

    public AttributeMap build() {
      if(empty) {
        return AttributeMap.EMPTY;
      }
      assert !reallyEmpty() : "empty flag is wrong";
      
      AttributeMap attributes = new AttributeMap();
      attributes.classes = classes;
      attributes.dim = dim;
      attributes.dimNames = dimNames;
      attributes.s4 = this.s4;

      if(names != null) {
        attributes.names = names;
      }

      if(map != null && !map.isEmpty()) {
        attributes.map = map;
      }
      return attributes;
    
    }
    
    private boolean reallyEmpty() {
      updateEmptyFlag();
      return empty;
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

      if(empty) {
        return AttributeMap.EMPTY;
      }

      assert !reallyEmpty() : "empty flag is wrong";

      AttributeMap attributes = new AttributeMap();
      attributes.s4 = s4;
      attributes.classes = classes;
      attributes.dim = validateDim(length);
      attributes.dimNames = validateDimNames();
      attributes.names = validateNames(length);

      if(map != null && !map.isEmpty()) {
        attributes.map = map;
      }
      return attributes;
    }

    private IntVector validateDim(int vectorLength) {

      if (dim == null) {
        return null;
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

      return (IntVector) dim;
    }

    private ListVector validateDimNames() {

      if(dimNames == null) {
        return null;
      }
      
      if(dimNames.length() == 0) {
        return null;
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
      
      return (StringVector) names;
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

  public static Builder newBuilderS4() {
    return new BuilderS4();
  }

  public static class BuilderS4 extends Builder {

    private boolean s4 = false;
    private StringVector classes = null;
    private SEXP names = null;
    private SEXP dim = null;
    private SEXP dimNames = null;

    private HashMap<Symbol, SEXP> map;

    private boolean empty = true;

    public BuilderS4() {
    }

    private BuilderS4(AttributeMap attributes) {
      this.s4 = attributes.s4;
      this.classes = attributes.classes;
      this.names = attributes.names;
      this.dim = attributes.dim;
      this.dimNames = attributes.dimNames;
      if(attributes.map != null) {
        this.map = new HashMap<>(attributes.map);
      }
      updateEmptyFlag();
    }

    public BuilderS4 setS4(boolean flag) {
      this.s4 = flag;
      updateEmptyFlag();
      return this;
    }

    public Builder setClass(String... classNames) {
      this.classes = new StringArrayVector(classNames);
      this.empty = false;
      return this;
    }

    /**
     * Validates a {@code class} attribute value
     *
     * @param value the proposed {@code class} attribute
     * @return the {@code classNames} vector, coerced to {@link StringVector} if not null.
     */
    public Builder setClass(SEXP value) {
      if(value.length() == 0) {
        return remove(Symbols.CLASS);
      }
      this.classes = toClassVector(value);
      this.empty = false;
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
          this.names = value;
          this.empty = false;

        } else if(name == Symbols.DIM) {
          this.dim = value;
          this.empty = false;

        } else if(name == Symbols.DIMNAMES) {
          this.dimNames =  value;
          this.empty = false;

        } else {
          this.empty = false;
          if(map == null) {
            map = Maps.newHashMap();
          }
          map.put(name, value);
        }
      }
      return this;
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
      } else if(map != null) {
        map.remove(name);
      }
      updateEmptyFlag();
      return this;
    }

    private void updateEmptyFlag() {
      this.empty = (!s4 && classes == null && dim == null && dimNames == null && names == null &&
          (map == null || map.isEmpty()));
    }

    public SEXP get(String what) {
      return get(Symbol.get(what));
    }

    public SEXP get(Symbol name) {
      if(name == Symbols.CLASS) {
        return classes;
      } else if(name == Symbols.NAMES) {
        return names;
      } else if(name == Symbols.DIM) {
        return dim;
      } else if(name == Symbols.DIMNAMES) {
        return dimNames;
      } else if(map != null && map.containsKey(name)) {
        return map.get(name);
      }
      return Null.INSTANCE;
    }

    public AttributeMap build() {
      if(empty) {
        return AttributeMap.EMPTY;
      }
      assert !reallyEmpty() : "empty flag is wrong";

      AttributeMap attributes = new AttributeMap();
      attributes.classes = classes;
      attributes.dim = dim;
      attributes.dimNames = dimNames;
      attributes.s4 = this.s4;

      if(names != null) {
        attributes.names = names;
      }

      if(map != null && !map.isEmpty()) {
        attributes.map = map;
      }
      return attributes;

    }

    private boolean reallyEmpty() {
      updateEmptyFlag();
      return empty;
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

      if(empty) {
        return AttributeMap.EMPTY;
      }

      assert !reallyEmpty() : "empty flag is wrong";

      AttributeMap attributes = new AttributeMap();
      attributes.s4 = s4;
      attributes.classes = classes;
      attributes.dim = dim;
      attributes.dimNames = dimNames;
      attributes.names = names;

      if(map != null && !map.isEmpty()) {
        attributes.map = map;
      }
      return attributes;
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
