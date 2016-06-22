package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;


public class DynamicArrayExpr implements GExpr {
  
  private JExpr array;
  private JExpr offset;

  public DynamicArrayExpr(JExpr array, JExpr offset) {
    this.array = array;
    this.offset = offset;
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrExpr(array, offset);
  }
}
