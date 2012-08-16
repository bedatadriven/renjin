package org.renjin.gcc.jimple;


import java.lang.reflect.Type;

public class JimpleType {
  private String name;

  public static final JimpleType INT = new JimpleType("int");
  public static final JimpleType BOOLEAN = new JimpleType("boolean");
  public static final JimpleType DOUBLE = new JimpleType("double");

  public JimpleType(String name) {
    this.name = name;
  }

  public JimpleType(Class clazz) {
    this.name = clazz.getName();
  }

  public JimpleType(Type type) {
    if(type instanceof Class) {
      this.name = ((Class) type).getName();
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
