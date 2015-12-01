package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;


public class MemCopyCallGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_memcpy";
  
  
  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {
    if(argumentGenerators.size() != 3) {
      throw new InternalCompilerException("__builtin_memcpy expects 3 args.");
    }
    ExprGenerator destination = argumentGenerators.get(0);
    ExprGenerator source = argumentGenerators.get(1);
    ExprGenerator length = argumentGenerators.get(2);

    source.emitPushPtrArrayAndOffset(mv);
    destination.emitPushPtrArrayAndOffset(mv);
    length.emitPrimitiveValue(mv);

    // public static native void arraycopy(
    //     Object src,  int  srcPos,
    // Object dest, int destPos,
    // int length);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "arraycopy", 
        Type.getMethodDescriptor(Type.VOID_TYPE, 
              Type.getType(Object.class), Type.INT_TYPE, 
              Type.getType(Object.class), Type.INT_TYPE,
              Type.INT_TYPE), false);

  }

  @Override
  public void emitCallAndPopResult(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {
    emitCall(mv, argumentGenerators);
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    return new VoidReturnGenerator().callExpression(this, argumentGenerators);
  }
}
