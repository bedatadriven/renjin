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
public class BitwiseShiftGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private final GimpleOp op;
  private final ValueGenerator x;
  private final ValueGenerator y;

  public BitwiseShiftGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = (ValueGenerator) x;
    this.y = (ValueGenerator) y;
  
    if(!checkTypes()) {
      throw new UnsupportedOperationException("Shift operations require types (int, int) or (long, int), found: " +
          this.x.getValueType() + ", " + this.y.getValueType());
    }
  }

  private boolean checkTypes() {
    Type tx = x.getValueType();
    Type ty = y.getValueType();
    
    return (tx.equals(Type.INT_TYPE) || tx.equals(Type.LONG_TYPE)) &&
           ty.equals(Type.INT_TYPE);
  }
  
  @Override
  public Type getValueType() {
    return x.getValueType();
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    x.emitPushValue(mv);
    y.emitPushValue(mv);
    
    mv.visitInsn(x.getValueType().getOpcode(op == GimpleOp.LSHIFT_EXPR ? ISHL : ISHR));
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }
}
