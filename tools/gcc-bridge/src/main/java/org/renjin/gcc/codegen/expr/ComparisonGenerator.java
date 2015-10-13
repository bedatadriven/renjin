package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.GimpleOp;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates codes for binary comparisons
 */
public class ComparisonGenerator implements ConditionGenerator {
  
  private GimpleOp op;
  private PrimitiveGenerator x;
  private PrimitiveGenerator y;

  public ComparisonGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = (PrimitiveGenerator) x;
    this.y = (PrimitiveGenerator) y;
  }

  @Override
  public void emitJump(MethodVisitor mv, Label trueLabel) {
    if (x.primitiveType().equals(Type.INT_TYPE) &&
        y.primitiveType().equals(Type.INT_TYPE)) {
      x.emitPush(mv);
      y.emitPush(mv);
      mv.visitJumpInsn(integerComparison(), trueLabel);
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported types: %s and %s",
            x.primitiveType(),
            y.primitiveType()));
    }
  }
  
  private int integerComparison() {
    switch (op) {
      case LT_EXPR:
        return IF_ICMPLT;
      case LE_EXPR:
        return IF_ICMPLE;
      case EQ_EXPR:
        return IF_ICMPEQ;
      case NE_EXPR:
        return IF_ICMPNE;
      case GT_EXPR:
        return IF_ICMPGT;
      case GE_EXPR:
        return IF_ICMPGE;
    }
    throw new UnsupportedOperationException("op: " + op);
  }

}
