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
public class LogicalOrGenerator extends AbstractExprGenerator {
  
  private ExprGenerator x;
  private ExprGenerator y;

  public LogicalOrGenerator(ExprGenerator x, ExprGenerator y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleBooleanType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    Label trueLabel = new Label();
    Label exitLabel = new Label();
    
    x.emitPrimitiveValue(mv);
    
    // if x is true, then can jump right away to true
    jumpIfTrue(mv, trueLabel);

    // Otherwise need to check y
    y.emitPrimitiveValue(mv);
    jumpIfTrue(mv, trueLabel);
    
    // FALSE: emit 0
    mv.visitLabel(trueLabel);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitJumpInsn(Opcodes.GOTO, exitLabel);
    
    // TRUE: emit 1
    mv.visitLabel(trueLabel);
    mv.visitInsn(Opcodes.ICONST_1);
    
    mv.visitLabel(exitLabel);
    
    
  }

  private void jumpIfTrue(MethodVisitor mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
}
