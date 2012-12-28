package org.renjin.eval;

import java.util.Map;
import java.util.Set;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;

import com.google.common.collect.Maps;

/**
 * Session-level options for 
 * @author Alex
 *
 */
public class Options {
  private Map<String, SEXP> map;

  public Options() {
    map = Maps.newHashMap();
    map.put("prompt", new StringArrayVector("> "));
    map.put("continue", new StringArrayVector("+ "));
    map.put("expressions" , new IntArrayVector(5000));
    map.put("width", new IntArrayVector(80));
    map.put("digits", new IntArrayVector(7));
    map.put("echo", new LogicalArrayVector(false));
    map.put("verbose", new LogicalArrayVector(false));
    map.put("check.bounds", new LogicalArrayVector(false));
    map.put("keep.source", new LogicalArrayVector(true));
    map.put("keep.source.pkgs", new LogicalArrayVector(false));
    map.put("warnings.length", new IntArrayVector(1000));
    map.put("OutDec", new StringArrayVector("."));
  }

  public SEXP get(String name) {
    SEXP value = map.get(name);
    return value == null ? Null.INSTANCE : value;
  }
  
  public int getInt(String name, int defaultValue) {
    SEXP value = get(name);
    if(value instanceof AtomicVector && value.length() >= 1) {
      return ((AtomicVector)value).getElementAsInt(0);
    }
    return defaultValue;
  }

  public SEXP set(String name, SEXP value) {
    SEXP old = map.put(name, value);
    return old == null ? Null.INSTANCE : value;
  }

  public Set<String> names() {
    return map.keySet();
  }

}