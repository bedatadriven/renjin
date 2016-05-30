package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.*;
import org.renjin.repackaged.guava.collect.ImmutableList;

import java.util.List;


public class WrapperType {

  public static final WrapperType OBJECT_PTR = new WrapperType(ObjectPtr.class);

  private static final List<WrapperType> TYPES = ImmutableList.of(
      new WrapperType(BytePtr.class),
      new WrapperType(IntPtr.class),
      new WrapperType(ShortPtr.class),
      new WrapperType(LongPtr.class),
      new WrapperType(BooleanPtr.class),
      new WrapperType(CharPtr.class),
      new WrapperType(DoublePtr.class),
      new WrapperType(FloatPtr.class),
      new WrapperType(ObjectPtr.class));
  


  /**
   * The internal class name of the {@code Ptr} class
   */
  private Type wrapperType;

  private final Type arrayType;
  
  private final Type baseType;

  public WrapperType(Class<? extends Ptr> wrapperClass) {
    try {

      Class<?> arrayClass = wrapperClass.getField("array").getType();

      this.wrapperType = Type.getType(wrapperClass);
      this.arrayType = Type.getType(arrayClass);
      this.baseType = Type.getType(arrayClass.getComponentType());
      
    } catch (Exception e) {
      throw new IllegalArgumentException(wrapperClass.getName());
    }
  }

  public Type getWrapperType() {
    return wrapperType;
  }

  public Type getArrayType() {
    return arrayType;
  }

  public Type getBaseType() {
    return baseType;
  }


  public GimpleType getGimpleType() {
    return new GimplePointerType(GimplePrimitiveType.fromJvmType(baseType));
  }


  public static boolean is(Type type) {
    for (WrapperType wrapperType : TYPES) {
      if (wrapperType.getWrapperType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  public static boolean is(Class<?> aClass) {
    return is(Type.getType(aClass));
  }

  public static WrapperType valueOf(Type type) {
    for (WrapperType wrapperType : TYPES) {
      if (wrapperType.getWrapperType().equals(type)) {
        return wrapperType;
      }
    }
    throw new IllegalArgumentException(type.toString());
  }

  @Override
  public String toString() {
    return wrapperType.getInternalName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WrapperType that = (WrapperType) o;

    return wrapperType.equals(that.wrapperType);
  }

  @Override
  public int hashCode() {
    return wrapperType.hashCode();
  }
}
