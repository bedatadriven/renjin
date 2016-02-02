package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.AddressOfPrimitiveValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.type.GimpleType;


public class LogicalNotGenerator implements Value {
  
  private Value operand;

  public LogicalNotGenerator(Value operand) {
    this.operand = operand;
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(MethodGenerator mv) {
    Label trueLabel = new Label();
    Label exit = new Label();

    operand.load(mv);
    mv.ifne(trueLabel);

    // operand is FALSE, push TRUE onto stack
    mv.iconst(1);
    mv.goTo(exit);

    // operand is TRUE, push FALSE onto stack
    mv.mark(trueLabel);
    mv.iconst(0);

    // Exit point
    mv.mark(exit);
  }
}
