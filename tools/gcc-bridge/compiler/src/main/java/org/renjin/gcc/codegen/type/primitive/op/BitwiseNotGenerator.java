package org.renjin.gcc.codegen.type.primitive.op;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;

public class BitwiseNotGenerator implements Value {

  private final Value argument;

  public BitwiseNotGenerator(Value argument) {
    this.argument = argument;
  }

  @Override
  public Type getType() {
    return argument.getType();
  }

  @Override
  public void load(MethodGenerator mv) {
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
