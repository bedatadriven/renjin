package r.jvmi.wrapper.generator.scalars;

import r.lang.LogicalVector;
import r.lang.Vector.Builder;

public class BooleanType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Boolean.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToBooleanPrimitive";
  }

  @Override
  public String getAccessorMethod() {
    return "isElementTrue";
  }

  @Override
  public Class getStorageType() {
    return Integer.TYPE;
  }  
  
  @Override
  public String getNALiteral() {
    return "IntVector.NA";
  }

  @Override
  public Class getVectorType() {
    return LogicalVector.class;
  }

  @Override
  public String convertToStorageTypeExpression(String valueExpression) {
    return "(" + valueExpression + " ? 1 : 0)";
  }

  @Override
  public Class<LogicalVector.Builder> getBuilderClass() {
    return LogicalVector.Builder.class;
  }

  @Override
  public String testExpr(String expr) {
    return "(" + expr + " instanceof Vector)";
  }
  
  
}
