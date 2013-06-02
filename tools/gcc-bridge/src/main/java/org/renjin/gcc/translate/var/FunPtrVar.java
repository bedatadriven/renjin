package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImFunctionPtrExpr;
import org.renjin.gcc.translate.type.ImFunctionPtrType;

/**
 * A variable which holds a pointer to a function
 */
public class FunPtrVar extends AbstractImExpr implements Variable, ImFunctionPtrExpr {

  private String jimpleName;
  private ImFunctionPtrType type;

  public FunPtrVar(FunctionContext context, String gimpleName, ImFunctionPtrType type) {
    this.type = type;
    this.jimpleName = Jimple.id(gimpleName);

    context.getBuilder().addVarDecl(type.interfaceType(), jimpleName);
  }

  @Override
  public ImFunctionPtrType type() {
    return type;
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    if(rhs.isNull()) {
      context.getBuilder().addStatement(Jimple.id(jimpleName) + " = null");

    } else if (rhs instanceof ImFunctionPtrExpr) {
      context.getBuilder().addStatement(jimpleName + " = " +
          ((ImFunctionPtrExpr) rhs).invokerReference(context));
    }
  }

  public JimpleExpr getJimpleVariable() {
    return new JimpleExpr(jimpleName);
  }

  @Override
  public JimpleExpr invokerReference(FunctionContext context) {
    return new JimpleExpr(jimpleName);
  }

  @Override
  public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
    if(className.equals(type.interfaceType().toString())) {
      return invokerReference(context);
    }
    throw new UnsupportedOperationException(className.toString()
    );
  }
}
