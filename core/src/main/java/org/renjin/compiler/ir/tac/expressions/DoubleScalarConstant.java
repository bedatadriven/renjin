package org.renjin.compiler.ir.tac.expressions;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.compiler.emit.EmitContext;

public class DoubleScalarConstant extends Constant {
  private final double value;

  public DoubleScalarConstant(double value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }


  @Override
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    if(value == 0) {
      mv.visitInsn(Opcodes.DCONST_0);
    } else if(value==1) {
      mv.visitInsn(Opcodes.DCONST_1);
    } else {
      mv.visitLdcInsn(value);
    }
    return 2;
  }

  @Override
  public String toString() {
    return value + "d";
  }
}
