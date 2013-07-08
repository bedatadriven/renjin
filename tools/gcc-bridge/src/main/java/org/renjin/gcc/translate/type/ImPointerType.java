package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.*;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.PtrVar;
import org.renjin.gcc.translate.var.Variable;

/**
 * A pointer type for the general case, i.e. a pointer to an
 * arbitrary other type.
 *
 * <p>Specific cases, like pointers to primitives and primitive arrays
 * are handled by other classes</p>
 */
public class ImPointerType implements ImIndirectType {

  private final ImType baseType;

  public ImPointerType(ImType baseType) {
    this.baseType = baseType;
  }

  @Override
  public JimpleType paramType() {
    return new RealJimpleType(ObjectPtr.class);
  }

  @Override
  public JimpleType returnType() {
    return new RealJimpleType(ObjectPtr.class);
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new PtrVar(functionContext, Jimple.id(gimpleName), this);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType pointerType() {
    return new ImPointerType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JimpleType getWrapperType() {
    return new RealJimpleType(ObjectPtr.class);
  }

  @Override
  public JimpleType getArrayType() {
    return new RealJimpleType(Object[].class);
  }
}
