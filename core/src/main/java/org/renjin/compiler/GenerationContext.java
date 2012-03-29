package org.renjin.compiler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.renjin.compiler.ir.tac.IRFunction;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GenerationContext {
  private String className;
  private SexpPool sexpPool;
  private ThunkMap thunkMap;
  private Map<String, IRFunction> closures = Maps.newHashMap();
  
  private int contextLdc = 1;
  private int environmentLdc = 2;
  
  public GenerationContext(String className, SexpPool sexpPool, ThunkMap thunkMap) {
    this.className = className;
    this.sexpPool = sexpPool;
    this.thunkMap = thunkMap;
  }
  
  public int getContextLdc() {
    return contextLdc;
  }
  
  public int getEnvironmentLdc() {
    return environmentLdc;
  }
    
  public void setContextLdc(int contextLdc) {
    this.contextLdc = contextLdc;
  }

  public void setEnvironmentLdc(int environmentLdc) {
    this.environmentLdc = environmentLdc;
  }
  
  public int getFirstFreeLocalVariable() {
    return Math.max(contextLdc, environmentLdc) + 1;
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
  
  public List<Entry<String, IRFunction>> getNestedClosures() {
    return Lists.newArrayList(closures.entrySet());
  }
}
