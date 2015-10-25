package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;


/**
 * Generates the result of a binary operation on two complex numbers
 * 
 */
public class ComplexBinOperator extends AbstractExprGenerator {

  private GimpleOp op;
  private GimpleComplexType type;
  private ExprGenerator x;
  private ExprGenerator y;

  public ComplexBinOperator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.type = (GimpleComplexType) x.getGimpleType();
    this.x = x;
    this.y = y;
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }

  @Override
  public ExprGenerator realPart() {
    return new RealPart();
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new ImPart();
  }

  private int typed(int opcode) {
    return type.getJvmPartType().getOpcode(opcode);
  }
  
  private class RealPart extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return type.getPartType();
    }

    @Override
    public Type getValueType() {
      return type.getJvmPartType();
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      switch (op) {
        case MULT_EXPR:
          //x.real * y.real - x.imaginary * y.rhs.imaginary
          x.realPart().emitPushValue(mv);
          y.realPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IMUL));

          x.imaginaryPart().emitPushValue(mv);
          y.imaginaryPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IMUL));

          mv.visitInsn(typed(Opcodes.ISUB));
          break;
        
        case PLUS_EXPR:
          x.realPart().emitPushValue(mv);
          y.realPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IADD));
          break;
        
        case MINUS_EXPR:
          x.realPart().emitPushValue(mv);
          y.realPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.ISUB));          
          break;
        
        default:
          throw new UnsupportedOperationException(op.name());
      }
    }
  }
  
  private class ImPart extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return type.getPartType();
    }

    @Override
    public Type getValueType() {
      return type.getJvmPartType();
    }

    @Override
    public void emitPushValue(MethodVisitor mv) {
      switch (op) {
        case MULT_EXPR:
          // x.real * y.imaginary + x.imaginary * y.real
          x.realPart().emitPushValue(mv);
          y.imaginaryPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IMUL));

          x.imaginaryPart().emitPushValue(mv);
          y.realPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IMUL));

          mv.visitInsn(typed(Opcodes.IADD));
          break;

        case PLUS_EXPR:
          x.imaginaryPart().emitPushValue(mv);
          y.imaginaryPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.IADD));
          break;

        case MINUS_EXPR:
          x.imaginaryPart().emitPushValue(mv);
          y.imaginaryPart().emitPushValue(mv);
          mv.visitInsn(typed(Opcodes.ISUB));
          break;
        
        default:
          throw new UnsupportedOperationException(op.name());
      }
    }
  }
}
