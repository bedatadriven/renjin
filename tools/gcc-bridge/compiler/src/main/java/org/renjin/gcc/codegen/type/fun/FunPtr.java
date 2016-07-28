package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.RefPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;


public class FunPtr implements RefPtrExpr {

  private JExpr methodHandleExpr;
  private FatPtrPair address;

  public FunPtr(JExpr methodHandleExpr) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = null;
  }

  public FunPtr(JExpr methodHandleExpr, FatPtrPair address) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = address;
  }

  public JExpr unwrap() {
    return methodHandleExpr;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    FunPtr rhsFunPtrExpr = (FunPtr) rhs;
    ((JLValue) methodHandleExpr).store(mv, rhsFunPtrExpr.methodHandleExpr);
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new InternalCompilerException("Not addressable");
    }
    return address;
  }
}
