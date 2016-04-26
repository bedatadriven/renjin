package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;

import javax.annotation.Nonnull;

/**
 * Array Simple Element
 */
class ArrayElement implements SimpleLValue, Addressable {
  private SimpleExpr array;
  private SimpleExpr offset;

  public ArrayElement(SimpleExpr array, SimpleExpr offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public Expr addressOf() {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    array.load(mv);
    offset.load(mv);
    mv.aload(getType());
  }

  @Override
  public void store(MethodGenerator mv, SimpleExpr value) {
    array.load(mv);
    offset.load(mv);
    value.load(mv);
    mv.astore(getType());
  }

  @Nonnull
  @Override
  public Type getType() {
    return array.getType().getElementType();
  }
}
