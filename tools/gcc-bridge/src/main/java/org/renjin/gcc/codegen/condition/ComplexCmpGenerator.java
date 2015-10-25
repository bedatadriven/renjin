package org.renjin.gcc.codegen.condition;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates a comparison of complex values
 */
public class ComplexCmpGenerator extends AbstractExprGenerator implements ConditionGenerator {


  private GimpleOp op;
  private ExprGenerator x;
  private ExprGenerator y;
  private GimpleComplexType type;
  
  public ComplexCmpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = x;
    this.y = y;
    this.type = (GimpleComplexType) x.getGimpleType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }


  @Override
  public void emitJump(MethodVisitor mv, Label trueLabel, Label falseLabel) {
    switch (op) {
      case EQ_EXPR:
        emitJumpIfEqual(mv, trueLabel, falseLabel);
        break;
      case NE_EXPR:
        emitJumpIfEqual(mv, falseLabel, trueLabel);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported comparison " + op + " between complex values");
    }
  }

  private void emitJumpIfEqual(MethodVisitor mv, Label equalLabel, Label notEqualLabel) {
    
    // First check real part 
    x.realPart().emitPrimitiveValue(mv);
    y.realPart().emitPrimitiveValue(mv);

    // Compare the two real values.
    // If they are equal, ZERO is pushed onto the stack
    emitCompareParts(mv);
    
    // If the real values are not equal 
    // then jump to false immediately, no need to check imaginary
    mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);

    // Now check imaginary part
    x.imaginaryPart().emitPrimitiveValue(mv);
    y.imaginaryPart().emitPrimitiveValue(mv);

    // Compare the two imaginary values.
    // If they are equal, ZERO is pushed onto the stack
    emitCompareParts(mv);

    // If the imaginary values are not equal 
    // then jump to false
    mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
    
    // if we reach here, then both real and imaginary parts are equal
    mv.visitJumpInsn(Opcodes.GOTO, equalLabel);
  }

  private void emitCompareParts(MethodVisitor mv) {
    if(type.getPartType().getPrecision() == 64) {
      mv.visitInsn(Opcodes.DCMPG);
    } else {
      mv.visitInsn(Opcodes.FCMPG);
    }
  }
}
