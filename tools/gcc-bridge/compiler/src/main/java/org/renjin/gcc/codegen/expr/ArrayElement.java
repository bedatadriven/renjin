package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;

import javax.annotation.Nonnull;

/**
 * Array Simple Element
 */
public class ArrayElement implements JLValue {
  private JExpr array;
  private JExpr offset;

  public ArrayElement(JExpr array, JExpr offset) {
    this.array = array;
    this.offset = offset;
  }
  
  @Override
  public void load(@Nonnull MethodGenerator mv) {
    array.load(mv);
    offset.load(mv);
    mv.aload(getType());
  }

  @Override
  public void store(MethodGenerator mv, JExpr value) {
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
