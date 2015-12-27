package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleType;


public class MinMaxGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimpleOp op;
  private ExprGenerator x;
  private ExprGenerator y;

  public MinMaxGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
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

    if(!x.getGimpleType().equals(y.getGimpleType())) {
      throw new UnsupportedOperationException(String.format(
          "Types must be the same: %s != %s", x.getGimpleType(), y.getGimpleType()));
    }
    
    Type type = x.getJvmPrimitiveType();

    if (type.equals(Type.LONG_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(JJ)J", false);

    } else if (type.equals(Type.FLOAT_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(FF)F", false);

    } else if (type.equals(Type.DOUBLE_TYPE)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(DD)D", false);

    } else {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(II)I", false);
    }
  }

  private String methodName() {
    switch (op) {
      case MAX_EXPR:
        return "max";
      case MIN_EXPR:
        return "min";
      default:
        throw new InternalCompilerException("op: " + op);
    }
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }

}
