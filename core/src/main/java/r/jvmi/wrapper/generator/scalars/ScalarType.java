package r.jvmi.wrapper.generator.scalars;

import r.lang.Vector;


public abstract class ScalarType {
  
  public abstract Class getScalarType();

  public abstract String getConversionMethod();
  
  public abstract String getAccessorMethod();

  public Class getStorageType() {
    return getScalarType();
  }

  public abstract Class getVectorType();
  
  public String convertToStorageTypeExpression(String valueExpression) {
    return valueExpression;
  }

  public abstract String getNALiteral();

  public abstract Class<? extends Vector.Builder<?>> getBuilderClass();
  
}
