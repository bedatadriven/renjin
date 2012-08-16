package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;

import java.util.List;

public abstract class Variable {

  public abstract void assign(GimpleOp op, List<GimpleExpr> operands);

  public JimpleExpr asNumericExpr() {
    throw new UnsupportedOperationException(this + " does not have a numeric representation");
  }

  public void initFromParameter() {

  }

}
