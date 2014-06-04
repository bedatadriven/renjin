package org.renjin.invoke.codegen.scalars;

import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;

public class LogicalType extends ScalarType {


  @Override
  public Class getScalarType() {
    return Logical.class;
  }

  @Override
  public String getConversionMethod() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsLogical";
  }

  @Override
  public Class getVectorType() {
    return LogicalVector.class;
  }

  @Override
  public Class<LogicalArrayVector.Builder> getBuilderClass() {
    return LogicalArrayVector.Builder.class;
  }


}
