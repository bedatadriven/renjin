package org.renjin.gcc.codegen.type.primitive.op;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;

import javax.annotation.Nonnull;

/**
 * TRUTH_XOR_EXPR
 */
public class LogicalXorGenerator implements Value {
  
  private final Value x;
  private final Value y;

  public LogicalXorGenerator(Value x, Value y) {
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
