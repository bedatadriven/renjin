package org.renjin.gcc.jimple;


import java.lang.reflect.Type;

public class JimpleType {
  private String name;

  public static final JimpleType INT = new JimpleType("int");
  public static final JimpleType BOOLEAN = new JimpleType("boolean");
  public static final JimpleType DOUBLE = new JimpleType("double");
  public static final JimpleType FLOAT = new JimpleType("float");
  public static final JimpleType LONG = new JimpleType("long");
  public static final JimpleType VOID = new JimpleType("void");

  public JimpleType(String name) {
    this.name = name;
  }

  public JimpleType(Class clazz) {
    if(clazz.isArray()) {
      JimpleType componentType = new JimpleType(clazz.getComponentType());
      this.name = componentType.toString() + "[]";
    } else {
      this.name = clazz.getName();
    }
  }

  public JimpleType(Type type) {
    if(type instanceof Class) {
      this.name = ((Class) type).getName();
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JimpleType other = (JimpleType) obj;
    return other.name.equals(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
