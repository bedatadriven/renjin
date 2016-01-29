package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * TRUTH_XOR_EXPR
 */
public class LogicalXorGenerator extends AbstractExprGenerator {
  
  private final ExprGenerator x;
  private final ExprGenerator y;

  public LogicalXorGenerator(ExprGenerator y, ExprGenerator x) {
    this.y = y;
    this.x = x;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleBooleanType();
  }

  @Override
  public void emitPrimitiveValue(MethodGenerator mv) {
    Label trueLabel = new Label();
    Label exitLabel = new Label();

    x.emitPrimitiveValue(mv);
    y.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.IXOR);
//    
//    // if x is true, then we need to check y to make sure it is false
//    jumpIfTrue(mv, trueLabel);
//
//    // Otherwise  if x is false we know the result will be false
//    mv.visitInsn(Opcodes.ICONST_0);
//    mv.visitJumpInsn(Opcodes.GOTO, exitLabel);
//    
//    // Otherwise need to check y if false --
//    // (if x and y are true, then the results is false
//    y.emitPrimitiveValue(mv);
//    jumpIfTrue(mv, trueLabel);
//
//    // FALSE: emit 0
//    mv.visitInsn(Opcodes.ICONST_0);
//    mv.visitJumpInsn(Opcodes.GOTO, exitLabel);
//
//    // TRUE: emit 1
//    mv.visitLabel(trueLabel);
//    mv.visitInsn(Opcodes.ICONST_1);
//
//    mv.visitLabel(exitLabel);
  }

  private void jumpIfTrue(MethodGenerator mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }


}
