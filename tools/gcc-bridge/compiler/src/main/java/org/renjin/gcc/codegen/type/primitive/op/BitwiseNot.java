package org.renjin.gcc.codegen.type.primitive.op;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

import javax.annotation.Nonnull;

public class BitwiseNot implements JExpr {

  private final JExpr argument;

  public BitwiseNot(JExpr argument) {
    this.argument = argument;
  }

  @Nonnull
  @Override
  public Type getType() {
    return argument.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // Unary bitwise complement operator is implemented
    // as an XOR operation with -1 (all bits set)
    argument.load(mv);
    
    if(argument.getType().equals(Type.INT_TYPE)) {
      mv.iconst(-1);
      mv.xor(Type.INT_TYPE);

    } else if (argument.getType().equals(Type.BYTE_TYPE)) {
      mv.iconst(0xFF);
      mv.xor(Type.BYTE_TYPE);

    } else if (argument.getType().equals(Type.CHAR_TYPE)) {
      // unsigned 16 bit
      mv.iconst(0xFFFF);
      mv.xor(Type.CHAR_TYPE);
      
    } else {
      throw new UnsupportedOperationException("Unsupported type for bitwise not operator: "  + argument.getType());
    }
  }
}
