package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
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
    return new PrimitiveConstGenerator(constant.getReal());
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new PrimitiveConstGenerator(constant.getIm());
  }
}
