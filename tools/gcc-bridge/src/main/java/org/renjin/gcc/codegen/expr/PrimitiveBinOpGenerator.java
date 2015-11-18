package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Printer;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates bytecode for a binary operation on primitives (IMUL, DMUL, IADD, etc)
 */
public class PrimitiveBinOpGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimpleOp op;
  private int opCode;
  private final ExprGenerator x;
  private final ExprGenerator y;

  public PrimitiveBinOpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.opCode = opCodeFor(op);
    this.x = x;
    this.y = y;

    checkTypes();
  }

  public ExprGenerator getX() {
    return x;
  }

  public ExprGenerator getY() {
    return y;
  }

  public GimpleOp getOp() {
    return op;
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
    Type tx = x.getJvmPrimitiveType();
    Type ty = y.getJvmPrimitiveType();

    if(!tx.equals(ty)) {
      throw new IllegalStateException(String.format(
          "Incompatible types for %s: %s != %s", Printer.OPCODES[opCode], tx, ty));
    }
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    x.emitPrimitiveValue(mv);
    y.emitPrimitiveValue(mv);
    mv.visitInsn(getJvmPrimitiveType().getOpcode(opCode));
  }

  @Override
  public String toString() {
    return "(" + x + " " + Printer.OPCODES[opCode] + " " + y + ")";
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }
}
