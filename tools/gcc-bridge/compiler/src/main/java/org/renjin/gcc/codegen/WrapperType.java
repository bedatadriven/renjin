package org.renjin.gcc.codegen;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;


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
  
  public static WrapperType forPointerType(GimpleIndirectType type) {
    return of(type.getBaseType());
  }

  public static Type wrapperType(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(BooleanPtr.class);
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(DoublePtr.class);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      return Type.getType(FloatPtr.class);
    
    } else if(type.equals(Type.INT_TYPE)) {
      return Type.getType(IntPtr.class);
    
    } else if(type.equals(Type.LONG_TYPE)) {
      return Type.getType(LongPtr.class);
      
    } else if(type.equals(Type.CHAR_TYPE)) {
      return Type.getType(CharPtr.class);
      
    } else if(type.equals(Type.BYTE_TYPE)) {
      return Type.getType(BytePtr.class);
    
    } else {
      return Type.getType(ObjectPtr.class);
    }
  }

  public static WrapperType of(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return new WrapperType(BooleanPtr.class);

    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return new WrapperType(DoublePtr.class);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      return new WrapperType(FloatPtr.class);

    } else if(type.equals(Type.INT_TYPE)) {
      return new WrapperType(IntPtr.class);

    } else if(type.equals(Type.LONG_TYPE)) {
      return new WrapperType(LongPtr.class);

    } else if(type.equals(Type.CHAR_TYPE)) {
      return new WrapperType(CharPtr.class);

    } else if(type.equals(Type.BYTE_TYPE)) {
      return new WrapperType(BytePtr.class);

    } else {
      return new WrapperType(ObjectPtr.class);
    }
  }
  
  public static WrapperType of(GimpleType baseType) {
    if(baseType instanceof GimplePrimitiveType) {
      return of(((GimplePrimitiveType) baseType).jvmType());
    } else if(baseType instanceof GimpleArrayType) {
      return of(((GimpleArrayType) baseType).getComponentType());
    } else {
      return new WrapperType(ObjectPtr.class);
    }
  }
  
  public static Type wrapperArrayType(Type baseType) {
    if(baseType.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(boolean[].class);

    } else if(baseType.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(double[].class);

    } else if (baseType.equals(Type.FLOAT_TYPE)) {
      return Type.getType(float[].class);

    } else if(baseType.equals(Type.INT_TYPE)) {
      return Type.getType(int[].class);

    } else if(baseType.equals(Type.LONG_TYPE)) {
      return Type.getType(long[].class);

    } else if(baseType.equals(Type.CHAR_TYPE)) {
      return Type.getType(char[].class);

    } else if(baseType.equals(Type.BYTE_TYPE)) {
      throw new UnsupportedOperationException("todo: BytePtr");

    } else {
      return Type.getType(Object[].class);
    }
  }
  
  public static Type wrapperType(GimpleType baseType) {
    if(baseType instanceof GimplePrimitiveType) {
      return wrapperType(((GimplePrimitiveType) baseType).jvmType());
    } else {
      return Type.getType(ObjectPtr.class);
    }
  }

  public static Type wrapperArrayType(GimpleType baseType) {
    if(baseType instanceof GimplePrimitiveType) {
      return wrapperArrayType(((GimplePrimitiveType) baseType).jvmType());
    } else {
      return Type.getType(Object[].class);
    }
  }


  public String getConstructorDescriptor() {
    return Type.getMethodDescriptor(Type.VOID_TYPE, arrayType, Type.INT_TYPE);
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

  public static WrapperType valueOf(Class<?> wrapperClass) {
    return valueOf(Type.getType(wrapperClass));
  }

  /**
   * Emits the bytecode to consume a reference to a wrapper instance on the stack
   * and push the array and offset onto the stack. ( wrapper pointer -> array, offset)
   */
  public void emitUnpackArrayAndOffset(MethodGenerator mv, Optional<Type> castTo) {
    
    // duplicate the wrapper instance so we can call GETFIELD twice.
    mv.visitInsn(DUP);

    // Consume the first reference to the wrapper type and push the array field on the stack
    mv.visitFieldInsn(GETFIELD, wrapperType.getInternalName(), "array", arrayType.getDescriptor());
    
    if(castTo.isPresent()) {
      mv.visitTypeInsn(Opcodes.CHECKCAST, castTo.get().getInternalName());
    }

    // (wrapper, array) -> (array, wrapper)
    mv.visitInsn(SWAP);
    
    // Consume the second reference 
    mv.visitFieldInsn(GETFIELD, wrapperType.getInternalName(), "offset", "I");
  }

  public void emitUnpackArrayAndOffset(MethodGenerator mv, WrapperType castTo) {
    Type wrapperArray = Type.getType("[" + castTo.getWrapperType().getDescriptor());
    emitUnpackArrayAndOffset(mv, Optional.of(wrapperArray));
  }
  
  public void emitUnpackArrayAndOffset(MethodGenerator mv) {
    emitUnpackArrayAndOffset(mv, Optional.<Type>absent());
  }

  public void emitInvokeUpdate(MethodGenerator mv) {
    mv.visitMethodInsn(INVOKEVIRTUAL, wrapperType.getInternalName(), "update", getConstructorDescriptor(), false);
  }

  @Override
  public String toString() {
    return wrapperType.getInternalName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WrapperType that = (WrapperType) o;

    return wrapperType.equals(that.wrapperType);
  }

  @Override
  public int hashCode() {
    return wrapperType.hashCode();
  }
}
