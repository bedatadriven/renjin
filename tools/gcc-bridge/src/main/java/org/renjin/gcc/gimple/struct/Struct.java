package org.renjin.gcc.gimple.struct;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

public abstract class Struct {

  public abstract JimpleExpr memberRef(JimpleExpr instanceExpr, String member, JimpleType jimpleType);

  public abstract void assignMember(FunctionContext context, JimpleExpr instance, String member, JimpleExpr jimpleExpr);

  public abstract String getFqcn();

  public JimpleType getJimpleType() {
    return new JimpleType(getFqcn());
  }

}
