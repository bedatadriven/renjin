package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.PrimitiveArrayPtrVar;
import org.renjin.gcc.translate.var.Variable;

/**
 * Pointer to a primitive array type.
 *
 * <p>
 * Pointers to arrays are subtly different than pointers to primitives, in I think
 * only one regard: pointer arithmetic applies to the array as a whole rather than
 * individual elements.
 *
 * <p>So in general, pointer arithmetic on pointers to arrays is not common, and we'll
 * take the shortcut of disallowing it for now</p>
 */
public class ImPrimitiveArrayPtrType implements ImIndirectType {
  private final ImPrimitiveArrayType baseType;

  public ImPrimitiveArrayPtrType(ImPrimitiveArrayType baseType) {
    this.baseType = baseType;
  }

  @Override
  public JimpleType paramType() {
    return baseType.componentType().getPointerWrapperType();
  }

  @Override
  public JimpleType returnType() {
    return baseType.componentType().getPointerWrapperType();
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new PrimitiveArrayPtrVar(functionContext, Jimple.id(gimpleName), this);
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

  public ImPrimitiveArrayType baseType() {
    return baseType;
  }

  @Override
  public JimpleType getWrapperType() {
    return baseType.componentType().getPointerWrapperType();
  }

  @Override
  public JimpleType getArrayType() {
    return baseType.componentType().jimpleArrayType();
  }
}
