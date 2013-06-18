package org.renjin.invoke.codegen.scalars;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.model.JvmMethod;
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
  public Class getVectorType() {
    return LogicalVector.class;
  }

  @Override
  public Class<LogicalArrayVector.Builder> getBuilderClass() {
    return LogicalArrayVector.Builder.class;
  }

  @Override
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, JvmMethod.Argument formal) {
    return sexpVariable._instanceof(codeModel.ref(Vector.class));
  }
}
