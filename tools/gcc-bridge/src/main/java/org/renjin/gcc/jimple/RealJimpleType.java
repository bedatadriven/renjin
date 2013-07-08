package org.renjin.gcc.jimple;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import org.renjin.gcc.runtime.Ptr;

/**
 * A reference to a jimple type (JVM class) that already
 * exists on the classpath in compiled form.
 */
public class RealJimpleType extends JimpleType {

  private final Class clazz;

  public RealJimpleType(Type type) {
    if (type instanceof Class) {
      this.clazz = ((Class) type);
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  public RealJimpleType(Class clazz) {
    this.clazz = clazz;
  }

  @Override
  public boolean isPointerWrapper() {
    return Ptr.class.isAssignableFrom(clazz);
  }

  @Override
  public Class asPrimitiveClass() {
    if (!clazz.isPrimitive()) {
      throw new UnsupportedOperationException();
    }
    return clazz;
  }

  @Override
  public boolean isPrimitive() {
    return clazz.isPrimitive();
  }

  @Override
  public boolean isAssignableFrom(Class otherClass) {
    return clazz.isAssignableFrom(otherClass);
  }

  @Override
  public boolean is(Class clazz) {
    return this.clazz.equals(clazz);
  }

  @Override
  public JimpleType arrayType() {
    return new RealJimpleType(Array.newInstance(clazz, 0).getClass());
  }

  @Override
  public String toString() {
    if (clazz.isArray()) {
      RealJimpleType componentType = new RealJimpleType(clazz.getComponentType());
      return componentType + "[]";
    } else {
      return clazz.getName();
    }
  }
}
