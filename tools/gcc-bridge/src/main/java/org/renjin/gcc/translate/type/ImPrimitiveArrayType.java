package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.PrimitiveArrayVar;
import org.renjin.gcc.translate.var.Variable;

public class ImPrimitiveArrayType implements ImType {

  private ImPrimitiveType componentType;
  private Integer lowerBound;
  private Integer upperBound;

  public ImPrimitiveArrayType(ImPrimitiveType componentType, Integer lowerBound, Integer upperBound) {
    this.componentType = componentType;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public JimpleType paramType() {
    throw new UnsupportedOperationException("arrays as parameters not supported");
  }

  public JimpleType asJimple() {
    return new RealJimpleType(componentType.getArrayClass());
  }

  @Override
  public JimpleType returnType() {
    return componentType().getArrayType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    return new PrimitiveArrayVar(functionContext, Jimple.id(gimpleName), this);
  }

  @Override
  public ImPrimitiveArrayPtrType pointerType() {
    return new ImPrimitiveArrayPtrType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException(this.toString());
  }

  public ImPrimitiveType componentType() {
    return componentType;
  }

  public int getLength() {
    return upperBound - lowerBound + 1;
  }

  public int getLowerBound() {
    return lowerBound;
  }
}
