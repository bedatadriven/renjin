package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.repackaged.asm.Type;

public class DereferencedFatPtr implements RefPtrExpr, FatPtr {

  private final ValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private Type wrapperType;

  public DereferencedFatPtr(JExpr array, JExpr offset, ValueFunction valueFunction) {
    if(!array.getType().equals(Type.getType("[Ljava/lang/Object;"))) {
      throw new IllegalArgumentException("array: " + array.getType());
    }

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
    return new FatPtrPair(array, offset);
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
    LocalVarAllocator.LocalVar wrapper = mv.getLocalVarAllocator().reserve(wrapperType);
    wrapper.store(mv, castedElement());

    return Wrappers.toPair(wrapper);
  }
  
  @Override
  public FatPtrPair toPair() {
    return Wrappers.toPair(castedElement());
  }

  
}
