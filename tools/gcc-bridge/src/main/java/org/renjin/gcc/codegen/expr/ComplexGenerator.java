package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Converts real value to a complex value
 */
public class ComplexGenerator extends AbstractExprGenerator {
  
  private ExprGenerator realGenerator;
  private GimpleComplexType complexType;
  
  public ComplexGenerator(ExprGenerator realGenerator) {
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
    return new PrimitiveConstValueGenerator(complexType.getPartType(), 0d);
  }
}
