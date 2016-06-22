package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;


public class VoidPtr implements RefPtrExpr {
  
  private JExpr objectRef;
  private FatPtrExpr address;

  public VoidPtr(JExpr objectRef, FatPtrExpr address) {
    this.objectRef = objectRef;
    this.address = address;
  }

  public VoidPtr(JExpr objectRef) {
    this.objectRef = objectRef;
    this.address = null;
  }

  public JExpr getObjectRef() {
    return objectRef;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) objectRef).store(mv, ((RefPtrExpr) rhs).unwrap());
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  @Override
  public JExpr unwrap() {
    return objectRef;
  }
}
