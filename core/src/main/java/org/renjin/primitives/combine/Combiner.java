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

  public Combiner add(Iterable<NamedValue> list) {
    return add("", list);
  }

  public Combiner add(String parentPrefix, Iterable<? extends NamedValue> list) {
    for(NamedValue namedValue : list) {
      String prefix = combinePrefixes(parentPrefix, Strings.nullToEmpty(namedValue.getName()));
      SEXP value = namedValue.getValue();

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
          add(prefix, ((ListVector) value).namedValues());
        } else {
          builder.addElements(prefix, (ListVector) value);
        }

      } else if(value instanceof PairList) {
        if(recursive) {
          add(prefix, ((PairList) value).nodes());
        } else {
          builder.addElements(prefix, ((PairList) value).toVector());
        }
      } else {
        builder.add(prefix, value);
      }
    }
    return this;
  }

  private String combinePrefixes(String a, String b) {
    assert a != null;
    assert b != null;

    if(!a.isEmpty() && !b.isEmpty()) {
      return a + "." + b;
    } else if(!Strings.isNullOrEmpty(a)) {
      return a;
    } else {
      return b;
    }
  }

  public Vector build() {
    return builder.build();
  }
}
