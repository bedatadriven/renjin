package org.renjin.primitives;

import com.google.common.collect.Maps;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.*;

import java.util.Map;
import java.util.Set;

public class Split {
  private Split() {}
  
  /**
   * Splits the 
   * @param indices
   * @param factors
   * @return
   */
  @Primitive
  public static ListVector split(Vector toSplit, IntVector factors) {
    assert toSplit.length() == factors.length();
    
    SplitMap map = new SplitMap(toSplit);
    for(int i=0;i!=factors.length();++i) {
      int split = factors.getElementAsInt(i);
      if(!IntVector.isNA(split)) {
        map.getSplitBuilder(split)
            .addFrom(toSplit, i);
      }
    }

    StringVector levels = (StringVector) factors.getAttributes().get(Symbols.LEVELS);
    
    ListVector.NamedBuilder resultList = new ListVector.NamedBuilder();
    for(Integer split : map.getKeys()) {
      resultList.add(levels.getElementAsString(split-1),
          map.getSplitBuilder(split).build());
    }
    return resultList.build();
  }
  
  private static class SplitMap {
    private Vector sourceVector;
    private Map<Integer, Vector.Builder> splits = Maps.newHashMap();
    
    public SplitMap(Vector sourceVector) {
      this.sourceVector = sourceVector;
    }
    
    public Vector.Builder getSplitBuilder(int value) {
      Vector.Builder builder = splits.get(value);
      if(builder == null) {
        builder = sourceVector.newBuilderWithInitialSize(0);
        splits.put(value, builder);
      }
      return builder;
    }
    
    public Set<Integer> getKeys() {
      return splits.keySet();
    }
  }
}
