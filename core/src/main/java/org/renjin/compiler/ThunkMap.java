package org.renjin.compiler;

import org.renjin.compiler.ir.tac.expressions.IRThunk;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ThunkMap {
  
  /**
   * Maps IR thunks to class names
   */
  private Map<IRThunk, String> map = Maps.newHashMap();
  
  private String classPrefix = "r/compiled/runtime/";
  
  public ThunkMap(String classPrefix) {
    super();
    this.classPrefix = classPrefix;
  }

  public String getClassName(IRThunk thunk) {
    String className = map.get(thunk);
    if(className == null) {
      className = classPrefix + map.size();
      map.put(thunk, className);
    }
    return className;
  }

  public Set<Entry<IRThunk, String>> entrySet() {
    return map.entrySet();
  }

  public Set<IRThunk> keySet() {
    return map.keySet();
  }
  
}
