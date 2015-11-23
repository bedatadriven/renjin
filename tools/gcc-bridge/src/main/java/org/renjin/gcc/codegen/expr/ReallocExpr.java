package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Builtins;


public class ReallocExpr extends AbstractExprGenerator {
  
  private ExprGenerator pointer;
  private ExprGenerator size;

  public ReallocExpr(ExprGenerator pointer, ExprGenerator size) {
    this.pointer = pointer;
    this.size = size;
  }


  @Override
  public GimpleType getGimpleType() {
    return pointer.getGimpleType();
  }
  
  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    // push [array, offset, newCount]
    pointer.emitPushPtrArrayAndOffset(mv);
    offsetToElements(size, pointer.getGimpleType().getBaseType().sizeOf()).emitPrimitiveValue(mv);
    
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Builtins.class), "realloc", 
        reallocDescriptor(), false);
    
    // new array on stack
    // offset is zero
    mv.visitInsn(Opcodes.ICONST_0);
    
  }

  private String reallocDescriptor() {
    Type arrayType = pointer.getPointerType().getArrayType();
    
    return Type.getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE, Type.INT_TYPE);
  }
}


