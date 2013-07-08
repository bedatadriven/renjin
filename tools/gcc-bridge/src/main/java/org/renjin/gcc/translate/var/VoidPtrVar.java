package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.type.ImVoidPtrType;


public class VoidPtrVar extends AbstractImExpr implements Variable {
  private FunctionContext context;
  private String jimpleName;

  public VoidPtrVar(FunctionContext context, String gimpleName) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
 

    context.getBuilder().addVarDecl(Object.class, jimpleName);
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType type() {
    return ImVoidPtrType.INSTANCE;
  }
}
