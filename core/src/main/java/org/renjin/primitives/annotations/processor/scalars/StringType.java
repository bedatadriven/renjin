package org.renjin.primitives.annotations.processor.scalars;

import org.renjin.primitives.annotations.CoerceLanguageToString;
import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;

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
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable,
      JvmMethod.Argument formal) {
    JExpression vectorTest = super.testExpr(codeModel, sexpVariable, formal);
    if(formal.isAnnotatedWith(CoerceLanguageToString.class)) {
      return vectorTest
          .cor(sexpVariable._instanceof(codeModel.ref(FunctionCall.class)))
          .cor(sexpVariable._instanceof(codeModel.ref(Symbol.class)));
    } else {
      return vectorTest;
    }
  }
  
  @Override
  public Class<StringArrayVector.Builder> getBuilderClass() {
    return StringArrayVector.Builder.class;
  }
}
