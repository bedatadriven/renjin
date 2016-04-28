package org.renjin.gcc.codegen.array;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import java.util.Collections;
import java.util.List;


public class ArrayValueFunction implements ValueFunction {

  private final GimpleArrayType arrayType;
  private final ValueFunction elementValueFunction;

  public ArrayValueFunction(GimpleArrayType arrayType, ValueFunction elementValueFunction) {
    this.arrayType = arrayType;
    this.elementValueFunction = elementValueFunction;
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public int getElementLength() {
    // TODO: is this correct?
    // For example:
    // int x[20];
    // int *p[20] = &x;

    return elementValueFunction.getElementLength();
  }

  @Override
  public int getElementSize() {
    return elementValueFunction.getElementSize();
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    FatPtrExpr fatPtrExpr = (FatPtrExpr) expr;
    return Collections.singletonList(fatPtrExpr.wrap());
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return elementValueFunction.getValueConstructor();
  }

}