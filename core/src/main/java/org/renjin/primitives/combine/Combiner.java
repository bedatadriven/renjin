package org.renjin.primitives.combine;

import com.google.common.base.Strings;
import org.renjin.sexp.*;


/**
 * Combines a set of vectors and objects into a new array-backed
 * list or vector.
 */
class Combiner {
  private boolean recursive;
  private CombinedBuilder builder;


  public Combiner(boolean recursive, CombinedBuilder builder) {
    this.recursive = recursive;
    this.builder = builder;
  }

  public Combiner add(Vector list) {
    return add("", list);
  }

  public Combiner add(String parentPrefix, Vector list) {
    AtomicVector names = list.getNames();
    for (int i = 0; i < list.length(); i++) {
      String prefix;
      if(names == Null.INSTANCE) {
        prefix = parentPrefix;
      } else {
        prefix = combinePrefixes(parentPrefix, names.getElementAsString(i));
      }
      SEXP value = list.getElementAsSEXP(i);

      addElement(prefix, value);
    }
    return this;
  }

  private Combiner add(String parentPrefix, PairList pairList) {
    for (PairList.Node node : pairList.nodes()) {
      String prefix;
      if(node.hasTag()) {
        prefix = combinePrefixes(parentPrefix, node.getName());
      } else {
        prefix = parentPrefix;
      }
      addElement(prefix, node.getValue());
    }
    return this;
  }

  private void addElement(String prefix, SEXP value) {
    if(value instanceof FunctionCall) {
      // even though we FunctionCalls are pairlists, we treat them specially in this context
      // and do not recurse into them, treating them as opaque objects
      builder.add(prefix, value);

    } else if(value instanceof AtomicVector ||
        value instanceof ExpressionVector) {

      // Expression vectors are also treated atypically here
      builder.addElements(prefix, (Vector) value);

    } else if(value instanceof ListVector) {
      if(recursive) {
        add(prefix, ((ListVector) value));
      } else {
        builder.addElements(prefix, (ListVector) value);
      }

    } else if(value instanceof PairList) {
      if(recursive) {
        add(prefix, ((PairList) value));
      } else {
        builder.addElements(prefix, ((PairList) value).toVector());
      }
    } else {
      builder.add(prefix, value);
    }
  }

  private String combinePrefixes(String prefix, String name) {
    assert prefix != null;
    
    if(prefix.isEmpty() && StringVector.isNA(name)) {
      return StringVector.NA;
    }

    if(StringVector.isNA(name)) {
      name = "NA";
    }
    
    if(!prefix.isEmpty() && !name.isEmpty()) {
      return prefix + "." + name;
    } else if(!Strings.isNullOrEmpty(prefix)) {
      return prefix;
    } else {
      return name;
    }
  }

  public Vector build() {
    return builder.build();
  }
}
