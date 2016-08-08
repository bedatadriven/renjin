package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.NotAddressableException;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

/**
 * FatPtr with a null value.
 */
public class NullFatPtr implements FatPtr {
  
  private ValueFunction valueFunction;

  public NullFatPtr(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
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
    return toPair().wrap();
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return toPair();
  }

  @Override
  public FatPtrPair toPair() {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JExpr nullArray = Expressions.nullRef(arrayType);
    return new FatPtrPair(valueFunction, nullArray);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    throw new UnsupportedOperationException("Not an LValue");
  }

  @Override
  public GExpr addressOf() {
    throw new NotAddressableException();
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    mv.goTo(label);
  }

  @Override
  public GExpr valueOf() {
    throw new UnsupportedOperationException("TODO");
  }
}
