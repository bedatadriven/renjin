package org.renjin.primitives.annotations.processor.scalars;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.CastStyle;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;

public class IntegerType extends ScalarType {

  @Override
  public Class getScalarType() {
    return Integer.TYPE;
  }

  @Override
  public String getConversionMethod() {
    return "convertToInt";
  }

  @Override
  public String getAccessorMethod() {
    return "getElementAsInt";
  }

  @Override
  public Class getVectorType() {
    return IntVector.class;
  }

  @Override
  public String getNALiteral() {
    return "IntVector.NA";
  }

  @Override
  public Class<IntArrayVector.Builder> getBuilderClass() {
    return IntArrayVector.Builder.class;
  }

  @Override
  public String testExpr(String expr, CastStyle castStyle) {
    // R language generally seems to allow implicit conversion of doubles
    // to ints
    if(castStyle == CastStyle.IMPLICIT) {
      return "(" + expr + " instanceof IntVector || " + expr + " instanceof DoubleVector || " + 
        expr + " instanceof LogicalVector)";
    } else {
      return "(" + expr + " instanceof IntVector)";
    }
  }

  @Override
  public JExpression testExpr(JCodeModel codeModel, JVar sexpVariable, CastStyle castStyle) {
    if(castStyle == CastStyle.IMPLICIT) {
      return sexpVariable._instanceof(codeModel.ref(IntVector.class))
              .cor(sexpVariable._instanceof(codeModel.ref(DoubleVector.class)))
              .cor(sexpVariable._instanceof(codeModel.ref(LogicalVector.class)));
    } else {
      return sexpVariable._instanceof(codeModel.ref(IntVector.class));
    }
  }
}

