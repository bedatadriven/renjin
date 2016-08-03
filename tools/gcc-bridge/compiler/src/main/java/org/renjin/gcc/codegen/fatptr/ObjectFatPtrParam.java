package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;


public class ObjectFatPtrParam implements FatPtr {
  
  private ValueFunction valueFunction;
  private final JLValue paramExpr;

  public ObjectFatPtrParam(ValueFunction valueFunction, JLValue paramExpr) {
    this.valueFunction = valueFunction;
    this.paramExpr = paramExpr;
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
    return paramExpr;
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtrPair toPair() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr addressOf() {
    throw new UnsupportedOperationException("TODO");
  }
}
