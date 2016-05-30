package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleAddressableExpr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;

public class RecordUnitPtrValueFunction implements ValueFunction {
  
  private Type recordType;

  public RecordUnitPtrValueFunction(Type recordType) {
    this.recordType = recordType;
  }

  @Override
  public Type getValueType() {
    return recordType;
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return 4;
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    SimpleExpr pointerValue = Expressions.elementAt(array, offset);
    FatPtrExpr pointerAddress = new FatPtrExpr(array, offset);
    
    return new SimpleAddressableExpr(pointerValue, pointerAddress);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    return Collections.singletonList((SimpleExpr)expr);
  }
}
