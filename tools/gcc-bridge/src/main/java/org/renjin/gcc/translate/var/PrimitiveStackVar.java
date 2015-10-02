package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.PrimitiveAssignment;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.PrimitiveLValue;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Writes jimple instructions to store and retrieve a single primitive numeric
 * value in a local JVM variable, allocated on the stack.
 */
public class PrimitiveStackVar extends AbstractImExpr implements Variable, PrimitiveLValue {

  private FunctionContext context;
  private String jimpleName;
  private ImPrimitiveType type;

  public PrimitiveStackVar(FunctionContext context, ImPrimitiveType type, String gimpleName) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    this.type = type;

    context.getBuilder().addVarDecl(type.getPrimitiveClass(), jimpleName);

    // we need to initialize otherwise jimple goes crazy
    context.getBuilder().addAssignment(jimpleName, type.literalExpr(0));
  }

  @Override
  public String toString() {
    return "stack:" + jimpleName;
  }

  @Override
  public void writePrimitiveAssignment(JimpleExpr expr) {
    context.getBuilder().addStatement(jimpleName + " = " + expr); 
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {

    PrimitiveAssignment.assign(context, this, rhs);
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    return type.castIfNeeded(
      new JimpleExpr(jimpleName),
      type());
  }

  @Override
  public ImPrimitiveType type() {
    return type;
  }
}
