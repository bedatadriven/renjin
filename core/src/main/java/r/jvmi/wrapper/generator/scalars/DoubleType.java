package r.jvmi.wrapper.generator.scalars;

import r.lang.DoubleVector;
import r.lang.Vector.Builder;

public class DoubleType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Double.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToDoublePrimitive";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsDouble";
  }

  @Override
  public Class getVectorType() {
    return DoubleVector.class;
  }

  @Override
  public String getNALiteral() {
    return "DoubleVector.NA";
  }

  @Override
  public Class<DoubleVector.Builder> getBuilderClass() {
    return DoubleVector.Builder.class;
  }

}
