package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.repackaged.guava.base.Optional;

import java.util.List;

public class EmptyRecordPtrValueFunction implements ValueFunction {
  @Override
  public Type getValueType() {
    return Type.getType(Object.class);
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
    return Expressions.elementAt(array, offset);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    throw new UnsupportedOperationException();
  }
}
