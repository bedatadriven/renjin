package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.Types;
import org.renjin.gcc.gimple.type.GimpleType;


public class MaxGenerator extends  AbstractExprGenerator implements ValueGenerator {
  
  private ValueGenerator x;
  private ValueGenerator y;

  public MaxGenerator(ExprGenerator x, ExprGenerator y) {
    this.x = (ValueGenerator) x;
    this.y = (ValueGenerator) y;
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }


  @Override
  public Type getValueType() {
    return x.getValueType();
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    if(Types.isInt(x) && Types.isInt(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
   
    } else if(Types.isLong(x) && Types.isLong(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(JJ)J", false);
   
    } else {
      throw new UnsupportedOperationException(String.format("max (%s, %s)", x.getGimpleType(), y.getGimpleType()));
    }
  }
}
