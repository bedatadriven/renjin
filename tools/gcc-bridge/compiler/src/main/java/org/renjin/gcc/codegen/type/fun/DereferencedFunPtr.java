package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;


public class DereferencedFunPtr extends AbstractExprGenerator {
  
  private ExprGenerator pointerPointer;

  public DereferencedFunPtr(ExprGenerator pointerPointer) {
    this.pointerPointer = pointerPointer;
  }

  @Override
  public GimpleType getGimpleType() {
    return pointerPointer.getGimpleType().getBaseType();
  }

  @Override
  public void emitPushMethodHandle(MethodGenerator mv) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public ExprGenerator addressOf() {
    return pointerPointer;
  }
}
