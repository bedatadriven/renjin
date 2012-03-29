package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.IntVector;

public class IntegerType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Integer.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToInt";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsInt";
  }

  @Override
  public Class getVectorType() {
    return IntVector.class;
  }

  @Override
  public String getNALiteral() {
    return "IntVector.NA";
  }

  @Override
  public Class<IntVector.Builder> getBuilderClass() {
    return IntVector.Builder.class;
  }

  @Override
  public String testExpr(String expr) {
    // R language generally seems to allow implicit conversion of doubles
    // to ints
    return "(" + expr + " instanceof IntVector || " + expr + " instanceof DoubleVector || " + 
      expr + " instanceof LogicalVector)";
  } 
}
