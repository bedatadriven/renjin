package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.struct.Struct;

import java.util.List;

public class StructPtrVar extends Variable {

  private GimpleStructType type;
  private Struct struct;
  private String gimpleName;
  private String jimpleName;
  private FunctionContext context;


  public StructPtrVar(FunctionContext context, String gimpleName, Struct struct) {
    this.struct = struct;
    this.gimpleName = gimpleName;
    this.jimpleName = Jimple.id(gimpleName);
    this.context = context;
    
    context.getBuilder().addVarDecl(struct.getJimpleType(), jimpleName);
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch(op) {
    case SSA_NAME:
      context.getBuilder().addStatement(jimpleName + " = " + operands.get(0).toString());
      break;
    default:
      throw new UnsupportedOperationException(op + " " + operands);      
    }

  }

  @Override
  public JimpleExpr memberRef(String member, JimpleType jimpleType) {
    return struct.memberRef(new JimpleExpr(jimpleName), member, jimpleType);
  }

  @Override
  public JimpleExpr returnExpr() {
    return new JimpleExpr(jimpleName);
  }
}
