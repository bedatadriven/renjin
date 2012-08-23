package org.renjin.primitives.annotations.processor.scalars;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.CastStyle;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Vector;

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
  public Class<LogicalArrayVector.Builder> getBuilderClass() {
    return LogicalArrayVector.Builder.class;
  }

  @Override
  public String testExpr(String expr, CastStyle castStyle) {
    return "(" + expr + " instanceof Vector)";
  }

  @Override
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, CastStyle castStyle) {
    return sexpVariable._instanceof(codeModel.ref(Vector.class));
  }
}
