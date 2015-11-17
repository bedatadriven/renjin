package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.Types;
import org.renjin.gcc.gimple.type.GimpleType;


public class MaxGenerator extends  AbstractExprGenerator implements ExprGenerator {
  
  private ExprGenerator x;
  private ExprGenerator y;

  public MaxGenerator(ExprGenerator x, ExprGenerator y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }
  
  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    
    x.emitPrimitiveValue(mv);
    y.emitPrimitiveValue(mv);
    
    if(Types.isInt(x) && Types.isInt(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
   
    } else if(Types.isLong(x) && Types.isLong(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(JJ)J", false);
   
    } else {
      throw new UnsupportedOperationException(String.format("max (%s, %s)", x.getGimpleType(), y.getGimpleType()));
    }
  }
}
