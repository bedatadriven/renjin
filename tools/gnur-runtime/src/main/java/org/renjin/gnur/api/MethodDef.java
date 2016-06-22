package org.renjin.gnur.api;


import org.renjin.gcc.runtime.BytePtr;

import java.lang.invoke.MethodHandle;

public class MethodDef {
  public byte[] name;
  public int name$offset;
  public int types[];
  public int types$offset;
  public int numArgs;
  public MethodHandle fun;
  
  public String getName() {
    return new BytePtr(name, name$offset).nullTerminatedString();
  }
  
  public void set(MethodDef o) {
    this.name = o.name;
    this.name$offset = o.name$offset;
    this.types = o.types;
    this.types$offset = o.types$offset;
    this.numArgs = o.numArgs;
    this.fun = o.fun;
  }
}
