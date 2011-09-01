package r.jvmi.wrapper.generator.scalars;

import r.lang.IntVector;

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

}
