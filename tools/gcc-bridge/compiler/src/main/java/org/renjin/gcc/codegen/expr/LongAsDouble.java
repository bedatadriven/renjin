package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class LongAsDouble implements JLValue {
  private JExpr longValue;

  public LongAsDouble(JExpr longValue) {
    this.longValue = longValue;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // Load long as double
    longValue.load(mv);
    mv.invokestatic(Double.class, "longBitsToDouble", 
        Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.LONG_TYPE));
  }

  @Override
  public void store(MethodGenerator mv, JExpr expr) {
    // Store double as long

    ((JLValue) longValue).store(mv, new DoubleAsLong(expr));
  }


  
}
