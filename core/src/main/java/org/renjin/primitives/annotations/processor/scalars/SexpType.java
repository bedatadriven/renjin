package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector.Builder;

public class SexpType extends ScalarType {

  @Override
  public Class getScalarType() {
    return SEXP.class;
  }

  @Override
  public String getConversionMethod() {
    throw new UnsupportedOperationException();  
  }

  @Override
  public String getAccessorMethod() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getVectorType() {
    return ListVector.class;
  }

  @Override
  public Class<? extends Builder<?>> getBuilderClass() {
    return ListVector.Builder.class;
  }

}
