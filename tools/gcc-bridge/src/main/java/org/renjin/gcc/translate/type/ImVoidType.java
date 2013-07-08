package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.Variable;

public class ImVoidType implements ImType {
  
  public static ImVoidType INSTANCE = new ImVoidType();
  
  private ImVoidType() {
    
  }

  @Override
  public JimpleType paramType() {
    throw new UnsupportedOperationException("parameters cannot have void type");
  }

  @Override
  public JimpleType returnType() {
    return JimpleType.VOID;
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    throw new UnsupportedOperationException("fields cannot have void type");
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    throw new UnsupportedOperationException("local variables cannot have void type");
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException("fields cannot have void type");
  }

  @Override
  public ImType pointerType() {
    return ImVoidPtrType.INSTANCE;
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException("arrays cannot have void type");
  }
}
