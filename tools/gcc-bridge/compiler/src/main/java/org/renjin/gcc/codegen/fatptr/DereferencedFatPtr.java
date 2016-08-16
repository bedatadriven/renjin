package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class DereferencedFatPtr implements RefPtrExpr, FatPtr {

  private final ValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private Type wrapperType;

  public DereferencedFatPtr(JExpr array, JExpr offset, ValueFunction valueFunction) {
    this.array = array;
    this.offset = offset;
    this.valueFunction = valueFunction;
    this.wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
  }

  private ArrayElement element() {
    return Expressions.elementAt(array, offset);
  }
  
  private JExpr castedElement() {
    return Expressions.cast(element(), wrapperType);
  }

  @Override
  public JExpr unwrap() {
    return element();
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    if(rhs instanceof FatPtr) {
      element().store(mv, ((FatPtr) rhs).wrap());

    } else {
      throw new UnsupportedOperationException("TODO: " + rhs.getClass().getName());
    }
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public Type getValueType() {
    return valueFunction.getValueType();
  }

  @Override
  public boolean isAddressable() {
    return false;
  }

  @Override
  public JExpr wrap() {
    return element();
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return Wrappers.toPair(mv, valueFunction, castedElement());
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    element().load(mv);
    mv.ifnull(label);
  }

  @Override
  public GExpr valueOf() {
    throw new UnsupportedOperationException("TODO");
  }
}
