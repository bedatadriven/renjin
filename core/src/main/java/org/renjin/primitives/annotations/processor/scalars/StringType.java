package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector.Builder;

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
  public String getNALiteral() {
    return "StringVector.NA";
  }

  @Override
  public Class<StringVector.Builder> getBuilderClass() {
    return StringVector.Builder.class;
  }
  
}
