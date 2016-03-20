package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
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
  public List<SimpleExpr> toArrayValues(Expr expr) {
    return Collections.singletonList((SimpleExpr) expr);
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return Optional.<SimpleExpr>of(new RecordConstructor(strategy));
  }

}
