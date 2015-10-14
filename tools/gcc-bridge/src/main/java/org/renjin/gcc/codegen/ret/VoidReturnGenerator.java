package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;

public class VoidReturnGenerator implements ReturnGenerator {

  @Override
  public Type type() {
    return Type.VOID_TYPE;
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    mv.visitInsn(Opcodes.RETURN);
  }
}
