package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;

/**
 * Generates bytecode for left and right bitwise shifts
 */
public class BitwiseShiftGenerator extends AbstractExprGenerator implements ExprGenerator {
  
  private final GimpleOp op;
  private final ExprGenerator x;
  private final ExprGenerator y;

  public BitwiseShiftGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = x;
    this.y = y;
  
    if(!checkTypes()) {
      throw new UnsupportedOperationException("Shift operations require types (int, int) or (long, int), found: " +
          this.x.getJvmPrimitiveType() + ", " + this.y.getJvmPrimitiveType());
    }
  }

  private boolean checkTypes() {
    Type tx = x.getJvmPrimitiveType();
    Type ty = y.getJvmPrimitiveType();
    
    return (tx.equals(Type.INT_TYPE) || tx.equals(Type.LONG_TYPE)) &&
           ty.equals(Type.INT_TYPE);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    x.emitPrimitiveValue(mv);
    y.emitPrimitiveValue(mv);
    
    mv.visitInsn(x.getJvmPrimitiveType().getOpcode(op == GimpleOp.LSHIFT_EXPR ? ISHL : ISHR));
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }
}
