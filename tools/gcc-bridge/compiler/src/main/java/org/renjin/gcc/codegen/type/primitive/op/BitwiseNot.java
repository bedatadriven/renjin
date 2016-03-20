package org.renjin.gcc.codegen.type.primitive.op;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;

public class BitwiseNot implements SimpleExpr {

  private final SimpleExpr argument;

  public BitwiseNot(SimpleExpr argument) {
    this.argument = argument;
  }

  @Nonnull
  @Override
  public Type getType() {
    return argument.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    if(!argument.getType().equals(Type.INT_TYPE)) {
      throw new UnsupportedOperationException("Bitwise not only supported for int32 operands.");
    }

    // Unary bitwise complement operator is implemented
    // as an XOR operation with -1 (all bits set)
    argument.load(mv);
    mv.iconst(-1);
    mv.xor(Type.INT_TYPE);
  }
}
