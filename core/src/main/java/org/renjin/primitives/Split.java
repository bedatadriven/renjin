package org.renjin.primitives;

import com.google.common.collect.Maps;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.util.Map;

public class Split {
  private Split() {}
  

  @Internal
  public static ListVector split(Vector toSplit, IntVector factors) {
    assert toSplit.length() == factors.length();
    
    Map<Integer, SplitBuilder> map = Maps.newHashMap();
    
    for(int i=0;i!=factors.length();++i) {
      int splitIndex = factors.getElementAsInt(i);
      if(!IntVector.isNA(splitIndex)) {
        SplitBuilder splitBuilder = map.get(splitIndex);
        if(splitBuilder == null) {
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
    private Vector.Builder values;
    private StringVector.Builder names;

    public SplitBuilder(Vector toSplit) {
      this.values = toSplit.newBuilderWithInitialCapacity(0);
      if(toSplit.getAttributes().hasNames()) {
        names = new StringArrayVector.Builder();
      }
    }
    
    public void add(Vector source, int sourceIndex) {
      values.addFrom(source, sourceIndex);
      if(names != null) {
        names.add(source.getName(sourceIndex));
      }
    }
    
    public Vector build() {
      if(names != null) {
        values.setAttribute(Symbols.NAMES, names.build());
      }
      return values.build();
    }
    
  }
  
}
