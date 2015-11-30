package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Logical binary operator, such as TRUTH_OR, TRUTH_AND
 */
public class LogicalAndGenerator extends AbstractExprGenerator {
  
  private ExprGenerator x;
  private ExprGenerator y;

  public LogicalAndGenerator(ExprGenerator x, ExprGenerator y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleBooleanType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    // if x is false, then can jump right away to false
    x.emitPrimitiveValue(mv);
    jumpIfFalse(mv, falseLabel);

    // Otherwise need to check y
    y.emitPrimitiveValue(mv);
    jumpIfFalse(mv, falseLabel);
    
    // TRUE: emit 1
    mv.visitLabel(falseLabel);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitJumpInsn(Opcodes.GOTO, exitLabel);
    
    // FALSE: emit 0
    mv.visitLabel(falseLabel);
    mv.visitInsn(Opcodes.ICONST_0);
    
    mv.visitLabel(exitLabel);
  }

  private void jumpIfFalse(MethodVisitor mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
}
