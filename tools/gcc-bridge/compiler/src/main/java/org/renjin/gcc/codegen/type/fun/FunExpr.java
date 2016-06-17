package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;

public class FunExpr implements GExpr {
  
  private JExpr methodHandle;

  public FunExpr(JExpr methodHandle) {
    this.methodHandle = methodHandle;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GExpr addressOf() {
    return new FunPtr(methodHandle);
  }
}
