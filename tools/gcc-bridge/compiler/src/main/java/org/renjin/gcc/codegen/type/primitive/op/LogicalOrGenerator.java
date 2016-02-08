package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;

import javax.annotation.Nonnull;

/**
 * Logical binary operator, such as TRUTH_OR, TRUTH_AND
 */
public class LogicalOrGenerator implements Value {
  
  private Value x;
  private Value y;

  public LogicalOrGenerator(Value x, Value y) {
    this.x = x;
    this.y = y;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    Label trueLabel = new Label();
    Label exitLabel = new Label();
    
    x.load(mv);
    
    // if x is true, then can jump right away to true
    jumpIfTrue(mv, trueLabel);

    // Otherwise need to check y
    y.load(mv);
    jumpIfTrue(mv, trueLabel);
    
    // FALSE: emit 0
    mv.iconst(0);
    mv.goTo(exitLabel);
    
    // TRUE: emit 1
    mv.mark(trueLabel);
    mv.iconst(1);
    
    mv.mark(exitLabel);
  }

  private void jumpIfTrue(MethodGenerator mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }
}
