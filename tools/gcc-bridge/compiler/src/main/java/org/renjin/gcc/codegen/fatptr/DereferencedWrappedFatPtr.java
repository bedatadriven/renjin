package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class DereferencedWrappedFatPtr implements FatPtr {

  private ValueFunction valueFunction;
  private WrappedFatPtrExpr address;

  public DereferencedWrappedFatPtr(ValueFunction valueFunction, WrappedFatPtrExpr address) {
    this.valueFunction = valueFunction;
    this.address = address;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof VoidPtr) {
      invokeSet(mv, ((VoidPtr) rhs).unwrap());
      
    } else if(rhs instanceof FatPtr) {
      invokeSet(mv, ((FatPtr) rhs).wrap());
      
    } else {
      throw new UnsupportedOperationException("TODO: rhs = " + rhs.getClass().getName());
    }
  }
  
  private void invokeSet(MethodGenerator mv, JExpr rhs) {
    // Invoke the set() method on the ObjectPtr
    JExpr wrapperInstance = address.wrap();

    wrapperInstance.load(mv);
    rhs.load(mv);

    mv.invokevirtual(wrapperInstance.getType(), "set",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
  }

  @Override
  public GExpr addressOf() {
    return address;
  }

  @Override
  public Type getValueType() {
    return valueFunction.getValueType();
  }

  @Override
  public boolean isAddressable() {
    return true;
  }

  @Override
  public JExpr wrap() {
    return address.valueExpr();
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return Wrappers.toPair(mv, valueFunction, address.valueExpr());
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf() {
    return valueFunction.dereference(this.address);
  }
}
