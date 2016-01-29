package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

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
  public void emitPrimitiveValue(MethodGenerator mv) {
    x.emitPrimitiveValue(mv);
    y.emitPrimitiveValue(mv);

    Type type = x.getJvmPrimitiveType();

    switch (op) {
      case LSHIFT_EXPR:
        // Shifting left has (bitwise) the same result on signed and unsigned integers
        mv.visitInsn(type.getOpcode(ISHL));
        break;

      case RSHIFT_EXPR:
        // For right shifts, we need to consider whether the type is signed
        GimpleIntegerType integerType = (GimpleIntegerType) x.getGimpleType();
        if(integerType.isUnsigned()) {
          mv.visitInsn(type.getOpcode(IUSHR));

        } else {
          mv.visitInsn(type.getOpcode(ISHR));
        }
        break;
      
      default:
        throw new UnsupportedOperationException("Op: " + op);
    }
  }
  
  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
}
