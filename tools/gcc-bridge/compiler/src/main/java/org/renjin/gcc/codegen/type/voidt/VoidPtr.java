package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.repackaged.asm.Label;


public class VoidPtr implements RefPtrExpr {
  
  private JExpr objectRef;
  private FatPtrPair address;

  public VoidPtr(JExpr objectRef, FatPtrPair address) {
    this.objectRef = objectRef;
    this.address = address;
  }

  public VoidPtr(JExpr objectRef) {
    this.objectRef = objectRef;
    this.address = null;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    JLValue lhs = (JLValue) this.objectRef;

    if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      lhs.store(mv, fatPtrExpr.wrap());
    } else {
      lhs.store(mv, ((RefPtrExpr) rhs).unwrap());
    }
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

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    objectRef.load(mv);
    mv.ifnull(label);
  }
}
