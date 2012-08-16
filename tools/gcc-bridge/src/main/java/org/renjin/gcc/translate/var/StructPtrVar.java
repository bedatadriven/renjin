package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.struct.Struct;
import org.renjin.gcc.gimple.type.GimpleStructType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

import java.util.List;

public class StructPtrVar extends Variable {

  private GimpleStructType type;
  private Struct struct;
  private String gimpleName;
  private String jimpleName;


  public StructPtrVar(FunctionContext context, String gimpleName, Struct struct) {
    this.struct = struct;
    this.gimpleName = gimpleName;
    this.jimpleName = Jimple.id(gimpleName);

    context.getBuilder().addVarDecl(struct.getJimpleType(), jimpleName);
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    throw new UnsupportedOperationException(op + " " + operands);
  }

  @Override
  public JimpleExpr memberRef(String member, JimpleType jimpleType) {
    return struct.memberRef(new JimpleExpr(jimpleName), member, jimpleType);
  }
}
