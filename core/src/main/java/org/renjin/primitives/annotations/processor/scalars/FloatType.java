package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;

public class FloatType extends ScalarType {
  

  @Override
  public Class getScalarType() {
    return Double.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToFloatPrimitive";
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
  public Class<DoubleArrayVector.Builder> getBuilderClass() {
    return DoubleArrayVector.Builder.class;
  }
}
