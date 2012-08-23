package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

public class StringType extends ScalarType {

  @Override
  public Class getScalarType() {
    return String.class;
  }

  @Override
  public String getConversionMethod() {
    return "convertToString";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsString";
  }

  @Override
  public Class getVectorType() {
    return StringVector.class;
  }

  @Override
  public Class<StringArrayVector.Builder> getBuilderClass() {
    return StringArrayVector.Builder.class;
  }
  
}
