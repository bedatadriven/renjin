package r.jvmi.wrapper.generator.scalars;

import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.Vector.Builder;

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
  public String getNALiteral() {
    return "Null.INSTANCE";
  }

  @Override
  public Class<? extends Builder<?>> getBuilderClass() {
    return ListVector.Builder.class;
  }

}
