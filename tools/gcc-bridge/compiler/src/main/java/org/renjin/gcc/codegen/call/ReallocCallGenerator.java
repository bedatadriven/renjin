package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Builtins;

import java.util.List;

/**
 * Handles calls to realloc()
 */
public class ReallocCallGenerator implements CallGenerator {
  @Override
  public void emitCall(MethodGenerator visitor, List<ExprGenerator> argumentGenerators) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitCallAndPopResult(MethodGenerator visitor, List<ExprGenerator> argumentGenerators) {
    // NOOP
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    ExprGenerator pointer = argumentGenerators.get(0);
    ExprGenerator size = argumentGenerators.get(1);
    
    return new ReallocExpr(pointer, size);
  }

  public static class ReallocExpr extends AbstractExprGenerator {
    
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
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
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
}
