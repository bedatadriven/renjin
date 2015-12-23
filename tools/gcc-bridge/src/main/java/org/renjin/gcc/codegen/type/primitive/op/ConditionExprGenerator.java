package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates a boolean value based on a condition
 */
public class ConditionExprGenerator extends AbstractExprGenerator {
  
  private ConditionGenerator condition;

  public ConditionExprGenerator(ConditionGenerator condition) {
    this.condition = condition;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleBooleanType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    // Push this value as a boolean on the stack.
    // Requires a jump
    Label trueLabel = new Label();
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    condition.emitJump(mv, trueLabel, falseLabel);

    // if false
    mv.visitLabel(falseLabel);
    mv.visitInsn(ICONST_0);
    mv.visitJumpInsn(GOTO, exitLabel);

    // if true
    mv.visitLabel(trueLabel);
    mv.visitInsn(ICONST_1);

    // done
    mv.visitLabel(exitLabel);
  }

}
