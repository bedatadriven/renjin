package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleStructType;

import java.util.List;

public class MappedStructPtrVar extends Variable {

  private GimpleStructType type;

  public MappedStructPtrVar(GimpleStructType type) {
    this.type = type;
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException(op + " " + operands);
  }
}
