package org.renjin.gcc.symbols;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.renjin.gcc.gimple.GimpleFunction;

public class UndefinedCollector {
  
  private Multimap<String, String> map = HashMultimap.create();
  
  public void add(String name, GimpleFunction function) {
    map.put(name, String.format("%s: %s", function.getUnit().getSourceName(),  function.getMangledName()));
  }
  
  public boolean isEmpty() {
    return map.isEmpty();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("The following undefined symbols were referenced:\n");
    for (String symbol : map.keySet()) {
      sb.append(symbol).append(":\n");
      for (String location : map.get(symbol)) {
        sb.append("   ").append(location).append("\n");
      }
    }
    return sb.toString();
  }
}
