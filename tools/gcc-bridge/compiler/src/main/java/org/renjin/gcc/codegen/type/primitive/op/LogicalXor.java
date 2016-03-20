package org.renjin.gcc.codegen.type.primitive.op;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;

/**
 * Generates the logical exclusive or of two operands
 */
public class LogicalXor implements SimpleExpr {
  
  private final SimpleExpr x;
  private final SimpleExpr y;

  public LogicalXor(SimpleExpr x, SimpleExpr y) {
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
