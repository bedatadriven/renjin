package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.type.GimpleType;


public class LogicalNotGenerator extends AbstractExprGenerator implements ExprGenerator {
  
  private ExprGenerator operand;

  public LogicalNotGenerator(ExprGenerator operand) {
    this.operand = operand;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    Label trueLabel = new Label();
    Label exit = new Label();
    
    operand.emitPrimitiveValue(mv);
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


  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
}
