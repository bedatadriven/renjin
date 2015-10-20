package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;

public class LogicalNotGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private ValueGenerator operand;

  public LogicalNotGenerator(ExprGenerator operand) {
    this.operand = (ValueGenerator) operand;
  }

  @Override
  public Type getValueType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    Label trueLabel = new Label();
    Label exit = new Label();
    
    operand.emitPushValue(mv);
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

  @Override
  public GimpleType getGimpleType() {
    return operand.getGimpleType();
  }
}
