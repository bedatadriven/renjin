package org.renjin.gcc.translate.var;


import java.util.List;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.struct.Struct;

public class StructVar extends Variable {

  private GimpleStructType type;
  private Struct struct;
  private String gimpleName;
  private String jimpleName;
  private FunctionContext context;

  public StructVar(FunctionContext context, String gimpleName, Struct struct) {
    this.context = context;
    this.struct = struct;
    this.gimpleName = gimpleName;
    this.jimpleName = Jimple.id(gimpleName);

    context.getBuilder().addVarDecl(struct.getJimpleType(), jimpleName);
    context.getBuilder().addStatement(jimpleName + " = new " + struct.getJimpleType());
    context.getBuilder().addStatement("specialinvoke " + jimpleName + ".<" + struct.getJimpleType() + ": void <init>()>()");
  }


  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException(op + " " + operands);
  }

  @Override
  public void assignMember(String member, GimpleOp operator, List<GimpleExpr> operands) {
    switch(operator) {
    case REAL_CST:
      doAssign(member, JimpleExpr.doubleConstant(operands.get(0)));
      break;
    case INTEGER_CST:
      doAssign(member, JimpleExpr.integerConstant(operands.get(0)));
      break;

    case VAR_DECL:
    case SSA_NAME:
      Variable var = context.lookupVar(operands.get(0));
      doAssign(member, var.asPrimitiveExpr(var.getPrimitiveType()));
      break;

    default:
      throw new UnsupportedOperationException(operator + " " + operands);
    }
  }

  private void doAssign(String member, JimpleExpr jimpleExpr) {
    struct.assignMember(context, new JimpleExpr(jimpleName), member, jimpleExpr);
  }

  @Override
  public JimpleExpr wrapPointer() {
    return new JimpleExpr(jimpleName);
  }


  @Override
  public JimpleExpr returnExpr() {
    return new JimpleExpr(jimpleName);
  }
}
