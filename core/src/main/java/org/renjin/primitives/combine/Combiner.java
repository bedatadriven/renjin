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
