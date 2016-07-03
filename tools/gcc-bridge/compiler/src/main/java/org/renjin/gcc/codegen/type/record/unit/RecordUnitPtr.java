package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.repackaged.asm.Type;

public class RecordUnitPtr implements RefPtrExpr {
  
  private JExpr ref;
  private FatPtrExpr address;

  public RecordUnitPtr(JExpr ref) {
    this.ref = ref;
  }

  public RecordUnitPtr(JExpr ref, FatPtrExpr address) {
    this.ref = ref;
    this.address = address;
  }

  public Type getJvmType() {
    return ref.getType();
  }
  
  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) ref).store(mv, ((RecordUnitPtr) rhs).ref);
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  public JExpr unwrap() {
    return ref;
  }

}
