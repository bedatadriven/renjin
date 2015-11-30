package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.Types;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleType;


public class MinMaxGenerator extends  AbstractExprGenerator implements ExprGenerator {

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
    
    if(Types.isInt(x) && Types.isInt(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(II)I", false);
   
    } else if(Types.isLong(x) && Types.isLong(y)) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", methodName(), "(JJ)J", false);
   
    } else {
      throw new UnsupportedOperationException(String.format("max (%s, %s)", x.getGimpleType(), y.getGimpleType()));
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
