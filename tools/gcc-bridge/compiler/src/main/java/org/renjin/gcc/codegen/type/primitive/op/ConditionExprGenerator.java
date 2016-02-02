package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates a boolean value based on a condition
 */
public class ConditionExprGenerator implements Value {
  
  private ConditionGenerator condition;

  public ConditionExprGenerator(ConditionGenerator condition) {
    this.condition = condition;
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(MethodGenerator mv) {
    
    // Push this value as a boolean on the stack.
    // Requires a jump
    Label trueLabel = new Label();
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    condition.emitJump(mv, trueLabel, falseLabel);

    // if false
    mv.mark(falseLabel);
    mv.iconst(0);
    mv.goTo(exitLabel);

    // if true
    mv.mark(trueLabel);
    mv.iconst(1);

    // done
    mv.mark(exitLabel);
  }

}
