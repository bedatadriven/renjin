package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleAddressableExpr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import java.util.Collections;
import java.util.List;

/**
 * Created by alex on 8-2-16.
 */
class PrimitiveValueFunction implements ValueFunction {

  private GimplePrimitiveType type;

  public PrimitiveValueFunction(GimplePrimitiveType type) {
    this.type = type;
  }

  @Override
  public Type getValueType() {
    return type.jvmType();
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getElementSize() {
    return type.sizeOf();
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    FatPtrExpr address = new FatPtrExpr(array, offset);
    SimpleExpr value = Expressions.elementAt(array, offset);

    return new SimpleAddressableExpr(value, address);
  }

  @Override
  public List<SimpleExpr> getDefaultValue() {
    return Collections.emptyList();
  }
}
