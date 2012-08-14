package org.renjin.gcc.jimple;


import org.renjin.gcc.runtime.Pointer;

import java.lang.reflect.Type;

public class JimpleType {
  private String name;

  public JimpleType(String name) {
    this.name = name;
  }

  public JimpleType(Class<Pointer> clazz) {
    this.name = clazz.getName();
  }

  public JimpleType(Type type) {
    this.name = type.toString();
  }

  @Override
  public String toString() {
    return name;
  }
}
