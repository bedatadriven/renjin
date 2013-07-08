package org.renjin.gcc.jimple;

public abstract class JimpleType {

  public static final JimpleType INT = new RealJimpleType(int.class);
  public static final JimpleType BOOLEAN = new RealJimpleType(boolean.class);
  public static final JimpleType DOUBLE = new RealJimpleType(double.class);
  public static final JimpleType FLOAT = new RealJimpleType(float.class);
  public static final JimpleType LONG = new RealJimpleType(long.class);
  public static final JimpleType VOID = new RealJimpleType(void.class);
  public static final JimpleType CHAR = new RealJimpleType(char.class);

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (obj instanceof JimpleType) {
      return obj.toString().equals(this.toString());
    }
    return false;
  }

  public boolean isPointerWrapper() {
    return false;
  }

  public boolean isPrimitive() {
    return false;
  }

  public Class asPrimitiveClass() {
    throw new UnsupportedOperationException();
  }

  public boolean isFunctionPointer() {
    return toString().startsWith("org.renjin.gcc.runtime.FunPtr");
  }

  public boolean isAssignableFrom(Class otherClass) {
    return false;
  }            
  
  public abstract boolean is(Class clazz);

  public static JimpleType valueOf(Class clazz) {
    return new RealJimpleType(clazz);
  }

  public abstract JimpleType arrayType();
}
