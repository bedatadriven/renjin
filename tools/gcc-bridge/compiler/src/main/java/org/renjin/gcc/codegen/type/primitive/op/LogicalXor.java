package org.renjin.gcc.codegen.type.primitive.op;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * Generates the logical exclusive or of two operands
 */
public class LogicalXor implements JExpr {
  
  private final JExpr x;
  private final JExpr y;

  public LogicalXor(JExpr x, JExpr y) {
    this.y = y;
    this.x = x;
    Preconditions.checkArgument(x.getType().equals(Type.BOOLEAN_TYPE) &&
                                y.getType().equals(Type.BOOLEAN_TYPE));
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);
    mv.xor(Type.INT_TYPE);
  }
}
