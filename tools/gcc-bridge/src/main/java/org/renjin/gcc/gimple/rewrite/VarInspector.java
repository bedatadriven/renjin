package org.renjin.gcc.gimple.rewrite;


import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

public class VarInspector extends GimpleVisitor {

  private String name;

  public VarInspector(String name) {
    this.name = name;
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {

  }

  @Override
  public void visitCall(GimpleCall gimpleCall) {
    visitLvalue(gimpleCall.getLhs());
    for(GimpleExpr argument : gimpleCall.getParams()) {
      visitRValue(argument);
    }
  }

  private void visitLvalue(GimpleLValue lhs) {

  }

  private void visitRValue(GimpleExpr argument) {

  }

  @Override
  public void visitConditional(GimpleConditional gimpleConditional) {
    super.visitConditional(gimpleConditional);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    super.visitReturn(gimpleReturn);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public void visitSwitch(GimpleSwitch gimpleSwitch) {
    super.visitSwitch(gimpleSwitch);    //To change body of overridden methods use File | Settings | File Templates.
  }
}
