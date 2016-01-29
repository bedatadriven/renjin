package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.type.GimpleType;


public class AbsGenerator extends AbstractExprGenerator implements ExprGenerator {

  private ExprGenerator x;

  public AbsGenerator(ExprGenerator x) {
    this.x = x;
  }

  @Override
  public GimpleType getGimpleType() {
    return x.getGimpleType();
  }


  @Override
  public void emitPrimitiveValue(MethodGenerator mv) {

    x.emitPrimitiveValue(mv);

    if(x.getJvmPrimitiveType().equals(Type.INT_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);

    } else if(x.getJvmPrimitiveType().equals(Type.DOUBLE_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(D)D", false);

    } else if(x.getJvmPrimitiveType().equals(Type.FLOAT_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);

    } else if(x.getJvmPrimitiveType().equals(Type.LONG_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(J)J", false);

    } else {
      throw new UnsupportedOperationException(String.format("abs (%s)", x.getJvmPrimitiveType()));
    }
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
  

}
