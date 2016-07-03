package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;


/**
 * Generates a comparison of complex values
 */
public class ComplexCmpGenerator implements ConditionGenerator {

  private GimpleOp op;
  private ComplexValue x;
  private ComplexValue y;
  private Type type;
  
  public ComplexCmpGenerator(GimpleOp op, ComplexValue x, ComplexValue y) {
    this.op = op;
    this.x = x;
    this.y = y;
    this.type = x.getComponentType();
  }


  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
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

  private void emitJumpIfEqual(MethodGenerator mv, Label equalLabel, Label notEqualLabel) {
    
    // First check real part 
    x.getRealJExpr().load(mv);
    y.getRealJExpr().load(mv);

    // Compare the two real values.
    // If they are equal, ZERO is pushed onto the stack
    mv.cmpg(type);
    
    // If the real values are not equal 
    // then jump to false immediately, no need to check imaginary
    mv.ifne(notEqualLabel);

    // Now check imaginary part
    x.getImaginaryJExpr().load(mv);
    y.getImaginaryJExpr().load(mv);

    // Compare the two imaginary values.
    // If they are equal, ZERO is pushed onto the stack
    mv.cmpg(type);
    
    // If the imaginary values are not equal 
    // then jump to false
    mv.ifne(notEqualLabel);
    
    // if we reach here, then both real and imaginary parts are equal
    mv.goTo(equalLabel);
  }

}
