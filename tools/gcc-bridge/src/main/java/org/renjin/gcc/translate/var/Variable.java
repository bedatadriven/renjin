package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;

import java.util.List;

public abstract class Variable {

  public abstract void assign(GimpleOp op, List<GimpleExpr> operands);

  public JimpleExpr asNumericExpr() {
    throw new UnsupportedOperationException(this + " does not have a numeric representation");
  }

  public void initFromParameter() {

  }

  public void assignMember(String member, GimpleOp operator, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException(this + " does not support member assignment");
  }

  public JimpleExpr memberRef(String member, JimpleType jimpleType) {
    throw new UnsupportedOperationException(this + " does not support member assignment");
  }

  public JimpleExpr addressOf() {
    throw new UnsupportedOperationException(this + " is not addressable");
  }
}
