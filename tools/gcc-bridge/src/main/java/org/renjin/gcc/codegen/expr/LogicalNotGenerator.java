package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class LogicalNotGenerator implements PrimitiveGenerator {
  
  private PrimitiveGenerator operand;

  public LogicalNotGenerator(ExprGenerator operand) {
    this.operand = (PrimitiveGenerator) operand;
  }

  @Override
  public Type primitiveType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    Label trueLabel = new Label();
    Label exit = new Label();
    
    operand.emitPush(mv);
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
    
    // operand is FALSE, push TRUE onto stack
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitJumpInsn(Opcodes.GOTO, exit);
    
    // operand is TRUE, push FALSE onto stack
    mv.visitLabel(trueLabel);
    mv.visitInsn(Opcodes.ICONST_0);
    
    // Exit point
    mv.visitLabel(exit);
  }
}
