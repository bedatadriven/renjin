package org.renjin.gcc.translate.struct;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

public abstract class Struct {

  public abstract JimpleExpr memberRef(JimpleExpr instanceExpr, String member, JimpleType jimpleType);

  public abstract void assignMember(FunctionContext context, JimpleExpr instance, String member, JimpleExpr jimpleExpr);

  public abstract JimpleType getJimpleType();


}
