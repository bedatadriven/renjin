package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.gimple.expr.GimpleComplexConstant;
import org.renjin.gcc.gimple.type.GimpleType;


public class ComplexConstGenerator extends AbstractExprGenerator {
  
  private GimpleComplexConstant constant;

  public ComplexConstGenerator(GimpleComplexConstant constant) {
    this.constant = constant;
  }

  @Override
  public GimpleType getGimpleType() {
    return constant.getType();
  }

  @Override
  public ExprGenerator realPart() {
    return new PrimitiveConstValueGenerator(constant.getReal());
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new PrimitiveConstValueGenerator(constant.getIm());
  }
}
