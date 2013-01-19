package org.renjin.sexp;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.eval.EvalException;
import org.renjin.sexp.PairList.Builder;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import java_cup.lexer;

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
 * are stored in this structure as direct pointers, others in an
 * IdentityHashMap.
 */
public class AttributeMap {
  private StringVector classes = null;
  private StringVector names = null;
  private IntVector dim = null;

  private Map<Symbol, SEXP> map;
  
  public static boolean CATCH_DEFINED = false;
  
  public static void catchDefined() {
    CATCH_DEFINED = true;
  }

  public static AttributeMap EMPTY = new AttributeMap();

  private AttributeMap() {
  }

  /**
   *
   * @return the <em>dim</em> attribute, or {@code Null.INSTANCE} if no
   * <em>dim</em> attribute is present.
   */
  public Vector getDim() {
    return dim == null ? Null.INSTANCE : dim;
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
    if(map != null) {
      list.addAll(map.keySet());
    }
    return list;
  }

  public PairList asPairList() {
    PairList.Builder list = new PairList.Builder();
    addTo(list);
    return list.build();
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
    if(map != null) {
      for(Map.Entry<Symbol, SEXP> entry : map.entrySet()) {
        list.add(entry.getKey(), entry.getValue());
      }
    }
  }


  public SEXP get(String what) {
    return get(Symbol.get(what));
  }
  
  public boolean has(Symbol name) {
    return get(name) != Null.INSTANCE;
  }

  public SEXP get(Symbol name) {
    if(name == Symbols.CLASS && classes != null) {
      return classes;
    } else if(name == Symbols.DIM && dim != null) {
      return dim;
    } else if(name == Symbols.NAMES && names != null) {
      return names;
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

  public boolean hasAnyBesidesName() {
    return map != null || classes != null|| dim != null;
  }

  public Iterable<PairList.Node> nodes() {
    return asPairList().nodes();
  }

  public int[] getDimArray() {
    return dim.toIntArray();
  }

  public Vector getDimNames() {
    if(map == null) {
      return Null.INSTANCE;
    } else {
      return (Vector) map.get(Symbols.DIMNAMES);
    }
  }

  public Vector getDimNames(int i) {
    if(map == null) {
      return Null.INSTANCE;
    }
    Vector vector = (Vector) map.get(Symbols.DIMNAMES);
    if(vector instanceof ListVector) {
      return (Vector)((ListVector) vector).getElementAsSEXP(0);
    }
    return Null.INSTANCE;
  }

  public static Builder builder() {
    return new Builder();
  }

  public boolean hasNames() {
    return names != null;
  }

  public StringVector getNames() {
    return names;
  }

  public boolean empty() {
    return this == EMPTY;
  }
  
  /**
   *
   * @return {@code true} if this map contains a <em>class</em> attribute.
   */
  public boolean hasClass() {
    return classes != null;
  }

  public SEXP getClassVector() {
    return classes == null ? Null.INSTANCE : classes;
  }

  public AtomicVector getNamesOrNull() {
    return names == null ? Null.INSTANCE : names;
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
          copy.map = Maps.newIdentityHashMap();
          copy.map.put(Symbols.DIMNAMES, dimnames);
        }
      }
      return copy;
    } else {
      return AttributeMap.EMPTY;
    }
  }

  /**
   *
   * @return a new {@code AttributeMap} containing the {@code dim}, {@code names}, and {@code dimnames}
   * attributes from <em>a</em> and <em>b</em>. If an attribute is defined in both, the value in <em>a</em>
   * takes precedence.
   */
  public static AttributeMap combineStructural(AttributeMap a, AttributeMap b) {
    Builder builder = new Builder();
    builder.addIfNotNull(b, Symbols.DIM);
    builder.addIfNotNull(b, Symbols.NAME);
    builder.addIfNotNull(b, Symbols.DIMNAMES);
    builder.addIfNotNull(a, Symbols.DIM);
    builder.addIfNotNull(a, Symbols.NAME);
    builder.addIfNotNull(a, Symbols.DIMNAMES);
    return builder.build();
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
      Builder builder = new Builder(y.getAttributes());
      builder.addAllFrom(x.getAttributes());
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
      return combineStructural(x.getAttributes(), y.getAttributes());
    }
  }


  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((classes == null) ? 0 : classes.hashCode());
    result = prime * result + ((dim == null) ? 0 : dim.hashCode());
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    result = prime * result + ((names == null) ? 0 : names.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeMap other = (AttributeMap) obj;
    if (classes == null) {
      if (other.classes != null)
        return false;
    } else if (!classes.equals(other.classes))
      return false;
    if (dim == null) {
      if (other.dim != null)
        return false;
    } else if (!dim.equals(other.dim))
      return false;
    if (map == null) {
      if (other.map != null)
        return false;
    } else if (!map.equals(other.map))
      return false;
    if (names == null) {
      if (other.names != null)
        return false;
    } else if (!names.equals(other.names))
      return false;
    return true;
  }



  public static class Builder {
    private StringVector classes = null;
    private StringVector names = null;
    private IntVector dim = null;

    private Map<Symbol, SEXP> map;

    private boolean empty = true;

    public Builder() {
    }

    private Builder(AttributeMap attributes) {
      this.classes = attributes.classes;
      this.names = attributes.names;
      this.dim = attributes.dim;
      if(attributes.map != null) {
        this.map = new IdentityHashMap<Symbol, SEXP>(attributes.map);
      }
      updateEmptyFlag();
    }

    public Builder set(String name, SEXP value) {
      return set(Symbol.get(name), value);
    }

    public Builder set(Symbol name, SEXP value) {
      if(CATCH_DEFINED && name.getPrintName().equals("defined")) {
        throw new EvalException(value.toString());
      }
      
      if(value == Null.INSTANCE) {
        return remove(name);
      } else {
        this.empty = false;
        if(name == Symbols.CLASS) {
          this.classes = (StringVector) value;
        } else if(name == Symbols.NAMES) {
          this.names = (StringVector) value;
        } else if(name == Symbols.DIM) {
          this.dim = (IntVector) value;
        } else {
          if(map == null) {
            map = Maps.newIdentityHashMap();
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
      } else if(map != null) {
        map.remove(name);
      }
      updateEmptyFlag();
      return this;
    }

    private void updateEmptyFlag() {
      this.empty = (classes == null && dim == null && names == null &&
              (map == null || map.isEmpty()));
    }

    public Builder removeDim() {
      return remove(Symbols.DIM);
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
      } else if(map != null && map.containsKey(name)) {
        return map.get(name);
      }
      return Null.INSTANCE;
    }

    public Builder setDim(IntVector dim) {
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
      this.names = names;
      this.empty = false;
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
        set(Symbols.DIMNAMES, new ListVector(names));
      } else {
        throw new IllegalArgumentException("" + names);
      }
      return this;
    }

    
    public Builder setClass(String... classNames) {
      this.classes = new StringArrayVector(classNames);
      this.empty = false;
      return this;
    }
    /**

     * Copies all non-null attributes from {@code attributes} to this {@code Builder}
     * @param attributes
     */
    public void addAllFrom(AttributeMap attributes) {
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
      if(attributes.map != null) {
        for(Map.Entry<Symbol, SEXP> entry : attributes.map.entrySet()) {
          if(this.map == null) {
            this.map = Maps.newIdentityHashMap();
          }
          this.map.put(entry.getKey(), entry.getValue());
          this.empty = false;
        }
      }
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
      } else {
        AttributeMap attributes = new AttributeMap();
        attributes.classes = classes;
        attributes.names = names;
        attributes.dim = dim;
        if(map != null && !map.isEmpty()) {
          attributes.map = map;
        }
        return attributes;
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
