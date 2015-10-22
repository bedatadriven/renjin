package org.renjin.gcc.codegen.ret;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;

public class ComplexReturnGenerator implements ReturnGenerator {
  @Override
  public Type type() {
    return Type.getType(double[].class);
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushComplexAsDoubleArray(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
}
