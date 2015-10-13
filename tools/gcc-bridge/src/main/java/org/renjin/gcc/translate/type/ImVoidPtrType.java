package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.Variable;
import org.renjin.gcc.translate.var.VoidPtrVar;

/**
 * Placeholder for void*, but not currently implemented in any meaningful way
 */
public class ImVoidPtrType implements ImType {

  public static final ImVoidPtrType INSTANCE = new ImVoidPtrType();

  private ImVoidPtrType() {

  }


  @Override
  public JimpleType paramType() {
    return new RealJimpleType(Object.class);
  }

  @Override
  public JimpleType returnType() {
    return new RealJimpleType(Ptr.class);
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new VoidPtrVar(functionContext, gimpleName);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }

}
