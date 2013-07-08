package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
* Created by IntelliJ IDEA.
* User: alexander
* Date: 6/25/13
* Time: 2:37 PM
* To change this template use File | Settings | File Templates.
*/
public class NewArrayExpr extends AbstractImExpr implements ImIndirectExpr {
  private final ImPrimitivePtrType type;
  private final JimpleExpr elementCountExpr;

  public NewArrayExpr(ImPrimitivePtrType type, JimpleExpr elementCountExpr) {
    this.type = type;
    this.elementCountExpr = elementCountExpr;
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    String arrayExpr = context.declareTemp(type.getArrayType());
    context.getBuilder().addAssignment(arrayExpr,
        new JimpleExpr(String.format("newarray (%s)[%s]", type.baseType().asJimple(), elementCountExpr)));

    return new ArrayRef(arrayExpr, 0);
  }

  @Override
  public ImIndirectType type() {
    return type;
  }
}
