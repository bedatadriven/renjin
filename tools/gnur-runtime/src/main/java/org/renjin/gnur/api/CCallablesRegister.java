package org.renjin.gnur.api;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

public class CCallablesRegister {
  static final Map<String,MethodHandle> callMap = new HashMap<>();
  private CCallablesRegister() {}

  private static class CCallablesRegisterHolder {
    private static final CCallablesRegister INSTANCE = new CCallablesRegister();
  }

  public static CCallablesRegister getInstance() {
    return CCallablesRegisterHolder.INSTANCE;
  }

  static void setCallable(String key, MethodHandle value) {
    callMap.put(key, value);
  }

  static MethodHandle getCallable(String key) {
    return callMap.get(key);
  }
}