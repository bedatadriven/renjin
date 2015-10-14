package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;
import org.renjin.gcc.gimple.GimpleOp;

/**
 * Generates bytecode for a binary operation on primitives (IMUL, DMUL, IADD, etc)
 */
public class BinaryOpGenerator implements PrimitiveGenerator {
  
  private int opCode;
  private final PrimitiveGenerator x;
  private final PrimitiveGenerator y;

  public BinaryOpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.opCode = opCodeFor(op);
    this.x = (PrimitiveGenerator) x;
    this.y = (PrimitiveGenerator) y;

    checkTypes();
  }

  private static int opCodeFor(GimpleOp op) {
    switch (op) {

      case MULT_EXPR:
        return Opcodes.IMUL;
      case PLUS_EXPR:
        return Opcodes.IADD;
      
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
    Type tx = x.primitiveType();
    Type ty = y.primitiveType();

    if(!tx.equals(ty)) {
      throw new IllegalStateException(String.format(
          "Incompatible types for %s: %s != %s", Printer.OPCODES[opCode], tx, ty));
    }
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    x.emitPush(mv);
    y.emitPush(mv);
    mv.visitInsn(primitiveType().getOpcode(opCode));
  }

  @Override
  public String toString() {
    return "(" + x + " " + Printer.OPCODES[opCode] + " " + y + ")";
  }

  @Override
  public Type primitiveType() {
    return x.primitiveType();
  }
}
