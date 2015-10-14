package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.GimpleOp;

import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;

/**
 * Generates bytecode for left and right bitwise shifts
 */
public class BitwiseShiftGenerator implements PrimitiveGenerator {
  
  private final GimpleOp op;
  private final PrimitiveGenerator x;
  private final PrimitiveGenerator y;

  public BitwiseShiftGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = (PrimitiveGenerator) x;
    this.y = (PrimitiveGenerator) y;
  
    if(!checkTypes()) {
      throw new UnsupportedOperationException("Shift operations require types (int, int) or (long, int), found: " +
          this.x.primitiveType() + ", " + this.y.primitiveType());
    }
  }

  private boolean checkTypes() {
    Type tx = x.primitiveType();
    Type ty = y.primitiveType();
    
    return (tx.equals(Type.INT_TYPE) || tx.equals(Type.LONG_TYPE)) &&
           ty.equals(Type.INT_TYPE);
  }
  
  @Override
  public Type primitiveType() {
    return x.primitiveType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    x.emitPush(mv);
    y.emitPush(mv);
    
    mv.visitInsn(x.primitiveType().getOpcode(opCode()));
  }
  
  private int opCode() {
    switch (op) {
      case LSHIFT_EXPR:
        return ISHL;
      case RSHIFT_EXPR:
        return ISHR;
    }
    throw new UnsupportedOperationException("not a bitwise op: " + op);
  }
}
