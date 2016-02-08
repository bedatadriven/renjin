package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.GimpleOp;

import javax.annotation.Nonnull;

/**
 * Generates bytecode for a binary operation on primitives (IMUL, DMUL, IADD, etc)
 */
public class PrimitiveBinOpGenerator implements Value {

  private int opCode;
  private final Value x;
  private final Value y;

  public PrimitiveBinOpGenerator(GimpleOp op, Value x, Value y) {
    this.opCode = opCodeFor(op);
    this.x = x;
    this.y = y;

    checkTypes();
  }

  @Nonnull
  @Override
  public Type getType() {
    return x.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);
    mv.visitInsn(getType().getOpcode(opCode));
  }

  private static int opCodeFor(GimpleOp op) {
    switch (op) {

      case MULT_EXPR:
        return Opcodes.IMUL;
      case PLUS_EXPR:
        return Opcodes.IADD;
      case MINUS_EXPR:
        return Opcodes.ISUB;
      
      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
        return Opcodes.IDIV;

      case TRUNC_MOD_EXPR:
        return Opcodes.IREM;
      
      case BIT_AND_EXPR:
        return Opcodes.IAND;
      case BIT_IOR_EXPR:
        return Opcodes.IOR;
      case BIT_XOR_EXPR:
        return Opcodes.IXOR;
    }
    return 0;
  }

  private void checkTypes() {
    Type tx = x.getType();
    Type ty = y.getType();

    if(!tx.equals(ty)) {
      throw new IllegalStateException(String.format(
          "Incompatible types for %s: %s != %s", Printer.OPCODES[opCode], tx, ty));
    }
  }

  @Override
  public String toString() {
    return "(" + x + " " + Printer.OPCODES[opCode] + " " + y + ")";
  }

}
