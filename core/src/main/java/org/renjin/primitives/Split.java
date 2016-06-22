package org.renjin.primitives;

import com.google.common.collect.Maps;
import org.renjin.invoke.annotations.Internal;
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
