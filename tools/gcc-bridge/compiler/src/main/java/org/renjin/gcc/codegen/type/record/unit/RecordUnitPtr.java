package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.type.record.RecordValue;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class RecordUnitPtr implements RefPtrExpr {
  
  private JExpr ref;
  private FatPtr address;

  public RecordUnitPtr(JExpr ref) {
    this.ref = ref;
  }

  public RecordUnitPtr(JExpr ref, FatPtr address) {
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

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    ref.load(mv);
    mv.ifnull(label);
  }

  @Override
  public GExpr valueOf() {
    return new RecordValue(ref);
  }
}
