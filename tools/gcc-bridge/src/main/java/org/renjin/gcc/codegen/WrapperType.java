package org.renjin.gcc.codegen;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;


public class WrapperType {
  
  private static final List<WrapperType> TYPES = ImmutableList.of(
      new WrapperType(BytePtr.class),
      new WrapperType(IntPtr.class),
      new WrapperType(LongPtr.class),
      new WrapperType(BooleanPtr.class),
      new WrapperType(CharPtr.class),
      new WrapperType(DoublePtr.class));
  

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

  public static Type wrapperType(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(BooleanPtr.class);
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(DoublePtr.class);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      throw new UnsupportedOperationException("todo: FloatPtr");
    
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
      throw new UnsupportedOperationException("todo: FloatPtr");

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
    } else {
      return new WrapperType(ObjectPtr.class);
    }
  }
  
  public static Type wrapperArrayType(Type baseType) {
    if(baseType.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(boolean[].class);

    } else if(baseType.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(double[].class);

    } else if(baseType.equals(Type.FLOAT_TYPE)) {
      throw new UnsupportedOperationException("todo: FloatPtr");

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


  public static WrapperType valueOf(Type type) {
    for (WrapperType wrapperType : TYPES) {
      if (wrapperType.getWrapperType().equals(type)) {
        return wrapperType;
      }
    }
    throw new IllegalArgumentException(type.toString());
  }

  /**
   * Emits the bytecode to consume a reference to a wrapper instance on the stack
   * and push the array and offset onto the stack. ( wrapper pointer -> array, offset)
   */
  public void emitUnpackArrayAndOffset(MethodVisitor mv) {
    
    // duplicate the wrapper instance so we can call GETFIELD twice.
    mv.visitInsn(DUP);

    // Consume the first reference to the wrapper type and push the array field on the stack
    mv.visitFieldInsn(GETFIELD, wrapperType.getInternalName(), "array", arrayType.getDescriptor());

    // Consume the second reference 
    mv.visitFieldInsn(GETFIELD, wrapperType.getInternalName(), "offset", "I");
  }


  /**
   * Emits the bytecode to consume a reference to the array 
   */
  public void emitPushWrapper(MethodVisitor mv, PtrGenerator ptrGenerator) {

    String wrapperClass = WrapperType.wrapperType(ptrGenerator.baseType()).getInternalName();

    // Create a new instance of the wrapper
    mv.visitTypeInsn(Opcodes.NEW, wrapperType.getInternalName());

    // Initialize it with the array and offset
    mv.visitInsn(Opcodes.DUP);
    ptrGenerator.emitPushArrayAndOffset(mv);
    mv.visitMethodInsn(INVOKESPECIAL, wrapperClass, "<init>", getConstructorDescriptor(), false);
  }


}
