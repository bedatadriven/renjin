package r.compiler;

import java.util.Map;

import com.google.common.collect.Maps;

import r.compiler.ir.tac.IRFunction;

public class GenerationContext {
  private String className;
  private SexpPool sexpPool;
  private ThunkMap thunkMap;
  private Map<String, IRFunction> closures = Maps.newHashMap();
  
  public GenerationContext(String className, ThunkMap thunkMap) {
    this.className = className;
    this.sexpPool = new SexpPool();
    this.thunkMap = thunkMap;
  }
  
  public String getClassName() {
    return className;
  }

  public SexpPool getSexpPool() {
    return sexpPool;
  }

  public ThunkMap getThunkMap() {
    return thunkMap;
  }
  
  public String addClosure(IRFunction fn) {
    String className = this.className + "$closure$" + closures.size();
    closures.put(className, fn);
    return className;
  }
}
