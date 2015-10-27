package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Determines whether {@code x} and {@code y} are "unordered", that is, 
 * if one or both arguments are NaN.
 */
public class UnorderedExprGenerator extends AbstractExprGenerator {
  
  private ExprGenerator x;
  private ExprGenerator y;

  public UnorderedExprGenerator(ExprGenerator x, ExprGenerator y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleBooleanType();
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {

    Label unordered = new Label();
    Label exit = new Label();
    
    emitIsNaN(mv, x);
    // 1 = not a number
    // 0 = not (not a number)
    // If not equal to zero, then x is 
    mv.visitJumpInsn(Opcodes.IFNE, unordered);
    
    // If x was not NaN, then we have to check y too
    emitIsNaN(mv, y);
    mv.visitJumpInsn(Opcodes.IFNE, unordered);
    
    // Ordered!
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitJumpInsn(Opcodes.GOTO, exit);
    
    // Unordered
    mv.visitLabel(unordered);
    mv.visitInsn(Opcodes.ICONST_1);
    
    // Exit
    mv.visitLabel(exit);
  }

  private void emitIsNaN(MethodVisitor mv, ExprGenerator x) {
    x.emitPrimitiveValue(mv);
    
    GimpleRealType type = (GimpleRealType) x.getGimpleType();
    switch (type.getPrecision()) {
      case 64:
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "isNaN", "(D)Z", false);
        break;
      case 32:
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "isNaN", "(F)Z", false);
        break;
    }
  }


}
