package r.compiler;

import java.util.Map;

import r.compiler.ir.tac.expressions.IRThunk;

import com.google.common.collect.Maps;

public class ThunkMap {
  
  /**
   * Maps IR thunks to class names
   */
  private Map<IRThunk, String> map = Maps.newHashMap();
  
  private String packagePrefix = "r/compiled/runtime/";
  
  public String getClassName(IRThunk thunk) {
    String className = map.get(thunk);
    if(className == null) {
      className = packagePrefix + map.size();
      map.put(thunk, className);
    }
    return className;
  }
  
}
