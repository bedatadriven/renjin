package org.renjin.invoke.codegen.scalars;

import org.renjin.sexp.RawVector;

public class ByteType extends ScalarType {

  @Override
  public Class getScalarType() {
    return byte.class;
  }

  @Override
  public String getConversionMethod() {
    return "convertToRawPrimitive";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsByte";
  }

  @Override
  public Class getVectorType() {
    return RawVector.class;
  }

  @Override
  public Class<RawVector.Builder> getBuilderClass() {
    return RawVector.Builder.class;
  }

}
