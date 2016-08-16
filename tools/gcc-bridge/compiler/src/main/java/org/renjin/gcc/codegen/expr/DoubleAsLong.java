package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class DoubleAsLong implements JLValue {
  
  private JExpr doubleValue;

  public DoubleAsLong(JExpr doubleValue) {
    assert doubleValue.getType() == Type.DOUBLE_TYPE;
    this.doubleValue = doubleValue;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // Load double as long
    doubleValue.load(mv);
    mv.invokestatic(Double.class, "doubleToRawLongBits",
        Type.getMethodDescriptor(Type.LONG_TYPE, Type.DOUBLE_TYPE));
  }


  @Override
  public void store(MethodGenerator mv, JExpr expr) {
    assert expr.getType() == Type.LONG_TYPE;
    ((JLValue)doubleValue).store(mv, new LongAsDouble(expr));
  }
}
