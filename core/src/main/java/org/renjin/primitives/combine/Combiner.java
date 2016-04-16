package org.renjin.primitives.combine;

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

  public Combiner add(ListVector list) {
    return add("", list);
  }

  public Combiner add(String parentPrefix, ListVector list) {

    StringVector names = CombinedNames.combine(parentPrefix, list);
    for (int i = 0; i < list.length(); i++) {

      String name = names.getElementAsString(i);
      SEXP value = list.getElementAsSEXP(i);

      addElement(name, value);
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

    } else if(value instanceof PairList.Node) {
      if(recursive) {
        add(prefix,  ((PairList.Node) value).toVector());
      } else {
        builder.addElements(prefix, ((PairList) value).toVector());
      }
    } else {
      builder.add(prefix, value);
    }
  }

  public Vector build() {
    return builder.build();
  }
}
