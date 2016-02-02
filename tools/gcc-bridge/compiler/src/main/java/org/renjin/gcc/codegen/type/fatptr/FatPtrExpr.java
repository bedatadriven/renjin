package org.renjin.gcc.codegen.type.fatptr;

import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;


public class FatPtrExpr implements ExprGenerator {

  private Value array;
  private Value offset;

  public FatPtrExpr(Value array, Value offset) {
    this.array = array;
    this.offset = offset;
  }
  
  
}
