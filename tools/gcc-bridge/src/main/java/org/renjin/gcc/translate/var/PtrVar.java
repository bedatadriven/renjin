package org.renjin.gcc.translate.var;


import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.AbstractImExpr;
import org.renjin.gcc.translate.expr.ArrayRef;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImIndirectExpr;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPointerType;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;

public class PtrVar extends AbstractImExpr implements Variable, ImIndirectExpr {

  private final String jimpleArrayName;
  private final String jimpleStartIndexName;
  private ImPointerType type;

  public PtrVar(FunctionContext context, String name, ImPointerType type) {
    this.type = type;
    jimpleArrayName = name;
    jimpleStartIndexName = name + "_offset";

    context.getBuilder().addVarDecl(Object[].class, jimpleArrayName);
    context.getBuilder().addVarDecl(JimpleType.INT, jimpleStartIndexName);
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    return new ArrayRef(jimpleArrayName, jimpleStartIndexName);
  }


  @Override
  public ImIndirectType type() {
    return type;
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    if(rhs instanceof ImIndirectExpr) {
      ImIndirectExpr ptr = (ImIndirectExpr) rhs;
      ArrayRef ref = ptr.translateToArrayRef(context);
      context.getBuilder().addAssignment(jimpleArrayName, ref.getArrayExpr());
      context.getBuilder().addAssignment(jimpleStartIndexName, ref.getIndexExpr());
    } else {
      throw new UnsupportedOperationException(rhs.toString());
    }
  }
}
