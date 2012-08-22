package org.renjin.sexp;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.eval.EvalException;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Stores the attributes of SEXP.
 *
 * The most commonly accessed attributes
 * are stored as direct pointers, others in an
 * IdentityHashMap.
 */
public class AttributeMap {
  private StringVector classes = null;
  private StringVector names = null;
  private IntVector dim = null;

  private Map<Symbol, SEXP> map;

  public static AttributeMap EMPTY = new AttributeMap();

  private AttributeMap() {
  }

  public Vector getDim() {
    return dim == null ? Null.INSTANCE : dim;
  }

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

  public static Builder builder() {
    return new Builder();
  }

  public boolean hasNames() {
    return names != null;
  }

  public StringVector getNames() {
    return names;
  }

  public boolean hasClass() {
    return classes != null;
  }

  public SEXP getClassVector() {
    return classes == null ? Null.INSTANCE : classes;
  }

  public AtomicVector getNamesOrNull() {
    return names == null ? Null.INSTANCE : names;
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
    }

    public Builder set(String name, SEXP value) {
      return set(Symbol.get(name), value);
    }

    public Builder set(Symbol name, SEXP value) {
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
      this.empty = (classes == null && dim == null && names == null && (map == null || map.isEmpty()));
      return this;
    }

    public Builder removeDim() {
      return remove(Symbols.DIM);
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
}
