package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleFieldBuilder;
import org.renjin.gcc.jimple.JimpleModifiers;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.var.Variable;

/**
 * Provides implementation for 2-dimensional primitive pointers like double**
 */
public class ImPrimitive2dPtrType implements ImIndirectType {
  
  private ImPrimitiveType baseType;

  public ImPrimitive2dPtrType(ImPrimitiveType baseType) {
    this.baseType = baseType;
  }

  @Override
  public JimpleType getWrapperType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JimpleType getArrayType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JimpleType paramType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JimpleType returnType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    JimpleFieldBuilder arrayField = classBuilder.newField();
    arrayField.setName(memberName);
    arrayField.setType(baseType.jimpleArrayType());
    if(isStatic) {
      arrayField.setModifiers(JimpleModifiers.PUBLIC);
    } else {
      arrayField.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    }

    JimpleFieldBuilder offsetField = classBuilder.newField();
    offsetField.setName(indexMemberName(memberName));
    offsetField.setType(JimpleType.INT);
    if(isStatic) {
      offsetField.setModifiers(JimpleModifiers.PUBLIC);
    } else {
      offsetField.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    }
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return null;
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    return null;
  }

  @Override
  public ImType pointerType() {
    return null;
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    return null;
  }
}
