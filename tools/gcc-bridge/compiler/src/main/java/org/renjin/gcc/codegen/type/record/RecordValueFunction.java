package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;

import java.util.Collections;
import java.util.List;


public class RecordValueFunction implements ValueFunction {
  
  private RecordClassTypeStrategy strategy;

  public RecordValueFunction(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Type getValueType() {
    return strategy.getJvmType();
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getElementSize() {
    return 4;
  }

  @Override
  public SimpleExpr dereference(SimpleExpr array, SimpleExpr offset) {
    return Expressions.elementAt(array, offset);
  }

  @Override
  public List<SimpleExpr> getDefaultValue() {
    return Collections.<SimpleExpr>singletonList(new RecordConstructor(strategy));
  }
}
