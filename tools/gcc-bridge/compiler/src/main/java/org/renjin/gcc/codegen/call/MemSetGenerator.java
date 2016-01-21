package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates bytecode for calls to memset()
 */
public class MemSetGenerator implements CallGenerator {
  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {
    // Not used
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void emitCallAndPopResult(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {
    ExprGenerator pointer = argumentGenerators.get(0);
    ExprGenerator byteValue = argumentGenerators.get(1);
    ExprGenerator length = argumentGenerators.get(2);

    // memset signature is (array, offset, byteValue, length)

    // push arguments on the stack
    pointer.emitPushPtrArrayAndOffset(mv);
    byteValue.emitPrimitiveValue(mv);
    length.emitPrimitiveValue(mv);
    invokeMemset(mv, pointer);
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    return new Expr(argumentGenerators);
  }
  
  private class Expr extends AbstractExprGenerator {
    
    private List<ExprGenerator> arguments;

    public Expr(List<ExprGenerator> arguments) {
      this.arguments = arguments;
    }

    @Override
    public GimpleType getGimpleType() {
      return arguments.get(0).getGimpleType();
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      ExprGenerator pointer = arguments.get(0);
      ExprGenerator byteValue = arguments.get(1);
      ExprGenerator length = arguments.get(2);

      // memset signature is (array, offset, byteValue, length)

      // push arguments on the stack
      pointer.emitPushPtrArrayAndOffset(mv);
      
      // duplicate it so that it's on the stack when we return
      mv.visitInsn(Opcodes.DUP2);
      
      byteValue.emitPrimitiveValue(mv);
      length.emitPrimitiveValue(mv);
      
      // invoke memset(), pointer+offset should remain on the stack
      invokeMemset(mv, pointer);
    }
  }


  private void invokeMemset(MethodVisitor mv, ExprGenerator pointer) {
    // compose the signature based on the arguments
    WrapperType wrapperType = WrapperType.forPointerType((GimpleIndirectType) pointer.getGimpleType());

    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
        wrapperType.getWrapperType().getInternalName(),
        "memset",
        Type.getMethodDescriptor(Type.VOID_TYPE, wrapperType.getArrayType(), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE),
        false);
  }

}
