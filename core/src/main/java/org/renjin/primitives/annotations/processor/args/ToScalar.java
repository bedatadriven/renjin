package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
import org.renjin.primitives.annotations.processor.scalars.ScalarType;
import org.renjin.primitives.annotations.processor.scalars.ScalarTypes;


public class ToScalar extends ArgConverterStrategy {
  private ScalarType scalarType;
  
  public ToScalar(Argument formal) {
    super(formal);
    this.scalarType = ScalarTypes.get(formal.getClazz());
  }

  public static boolean accept(Argument formal) {
    return ScalarTypes.has(formal.getClazz());
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return scalarType.getConversionMethod() + "(" + argumentExpression + ")";
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return parent.classRef(WrapperRuntime.class).staticInvoke(scalarType.getConversionMethod())
            .arg(sexp);
  }

  @Override
  public String getTestExpr(String argLocal) {
    return scalarType.testExpr(argLocal);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return scalarType.testExpr(codeModel, sexpVariable);
  }
}
