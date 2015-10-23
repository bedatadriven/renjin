package org.renjin.gcc.codegen.ret;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;

public class ComplexReturnGenerator implements ReturnGenerator {
  
  private GimpleComplexType type;

  public ComplexReturnGenerator(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public Type type() {
    return type.getJvmPartArrayType();
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushComplexAsArray(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
}
