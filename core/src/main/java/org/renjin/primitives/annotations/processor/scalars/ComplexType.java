package org.renjin.primitives.annotations.processor.scalars;

import org.apache.commons.math.complex.Complex;

import r.lang.ComplexVector;

public class ComplexType extends ScalarType{

  
  @Override
  public Class<?> getScalarType() {
    return Complex.class;
  }

  @Override
  public String getConversionMethod() {
    return "convertToComplexPrimitive";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsComplex";
  }

  @Override
  public Class getVectorType() {
    return ComplexVector.class;
  }

  @Override
  public String getNALiteral() {
    return "ComplexVector.NA";
  }

  @Override
  public Class<ComplexVector.Builder> getBuilderClass() {
    return ComplexVector.Builder.class;
  }

}
