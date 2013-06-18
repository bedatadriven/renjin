package org.renjin.invoke.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.annotations.processor.ApplyMethodContext;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.invoke.annotations.processor.WrapperRuntime;
import org.renjin.invoke.annotations.processor.scalars.ScalarType;
import org.renjin.invoke.annotations.processor.scalars.ScalarTypes;


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
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return parent.classRef(WrapperRuntime.class).staticInvoke(scalarType.getConversionMethod())
            .arg(sexp);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return scalarType.testExpr(codeModel, sexpVariable, this.formal);
  }
}
