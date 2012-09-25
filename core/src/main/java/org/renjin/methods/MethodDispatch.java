package org.renjin.methods;

import java.util.HashMap;

import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import com.google.common.collect.Maps;

public class MethodDispatch {
  private boolean enabled = false;
  private HashMap<String, SEXP> extendsTable = Maps.newHashMap();
  
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public SEXP getExtends(String className) {
    SEXP value = extendsTable.get(className);
    if(value == null) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }
  
  public void putExtends(String className, SEXP klass) {
    extendsTable.put(className, klass);
  }
  
}
