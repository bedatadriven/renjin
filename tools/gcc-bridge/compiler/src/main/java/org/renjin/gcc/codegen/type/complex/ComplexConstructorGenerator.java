package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Converts real value to a complex value
 */
public class ComplexConstructorGenerator extends AbstractExprGenerator {
  
  private ExprGenerator realGenerator;
  private GimpleComplexType complexType;
  
  public ComplexConstructorGenerator(ExprGenerator realGenerator) {
    this.realGenerator = realGenerator;
    this.complexType = new GimpleComplexType((GimpleRealType) realGenerator.getGimpleType());
  }

  @Override
  public GimpleType getGimpleType() {
    return complexType;
  }

  @Override
  public ExprGenerator realPart() {
    return realGenerator;
  }

  @Override
  public ExprGenerator imaginaryPart() {
    return new PrimitiveConstGenerator(complexType.getPartType(), 0d);
  }
}
