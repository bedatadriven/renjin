package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.struct.Struct;
import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

import java.util.List;

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
    context.getBuilder().addStatement(jimpleName + " = new " + struct.getFqcn());
    context.getBuilder().addStatement("specialinvoke " + jimpleName + ".<" + struct.getFqcn() + ": void <init>()>()");
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

    case SSA_NAME:
      doAssign(member, context.lookupVar(operands.get(0)).asNumericExpr());
      break;

    default:
      throw new UnsupportedOperationException();
    }
  }

  private void doAssign(String member, JimpleExpr jimpleExpr) {
    struct.assignMember(context, new JimpleExpr(jimpleName), member, jimpleExpr);
  }

  @Override
  public JimpleExpr addressOf() {
    return new JimpleExpr(jimpleName);
  }
}
