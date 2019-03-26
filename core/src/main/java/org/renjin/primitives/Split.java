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
package org.renjin.primitives;

import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.Map;

public class Split {
  private Split() {}


  @Internal
  public static ListVector split(Vector toSplit, IntVector factors) {
    Map<Integer, SplitBuilder> map = Maps.newHashMap();

    int length = Math.max(toSplit.length(), factors.length());

    for(int i=0;i!=length;++i) {
      int factorIndex = i % factors.length();
      int splitIndex = factors.getElementAsInt(factorIndex);
      if(!IntVector.isNA(splitIndex)) {
        SplitBuilder splitBuilder = map.get(splitIndex);
        if (splitBuilder == null) {
          splitBuilder = new SplitBuilder(toSplit);
          map.put(splitIndex, splitBuilder);
        }
        splitBuilder.add(toSplit, i);
      }
    }

    StringVector levels = (StringVector) factors.getAttributes().get(Symbols.LEVELS);

    ListVector.NamedBuilder resultList = new ListVector.NamedBuilder();
    for(Integer split : map.keySet()) {
      resultList.add(levels.getElementAsString(split-1), map.get(split).build());
    }

    return resultList.build();
  }


  private static class SplitBuilder {
    private Vector.Builder source;
    private AtomicVector sourceNames;
    private StringVector.Builder names;

    public SplitBuilder(Vector toSplit) {
      this.source = toSplit.newBuilderWithInitialCapacity(0);
      this.sourceNames = toSplit.getNames();
      if(sourceNames != Null.INSTANCE) {
        names = new StringArrayVector.Builder();
      }
    }

    public void add(Vector source, int sourceIndex) {
      if(sourceIndex < source.length()) {
        this.source.addFrom(source, sourceIndex);
        if (names != null) {
          names.add(sourceNames.getElementAsString(sourceIndex));
        }
      }
    }

    public Vector build() {
      if(names != null) {
        source.setAttribute(Symbols.NAMES, names.build());
      }
      return source.build();
    }

  }

}
