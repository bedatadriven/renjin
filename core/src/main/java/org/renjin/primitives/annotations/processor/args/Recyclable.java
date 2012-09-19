package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
import org.renjin.primitives.annotations.processor.scalars.ScalarType;
import org.renjin.primitives.annotations.processor.scalars.ScalarTypes;

import static com.sun.codemodel.JExpr.lit;


public class Recyclable extends ArgConverterStrategy {

  private ScalarType scalarType;
  
  public Recyclable(Argument formal) {
    super(formal);
    this.scalarType = ScalarTypes.get(formal.getClazz());
  }

  public static boolean accept(Argument formal) {
    return formal.isRecycle();
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexp) {
    return sexp.invoke("length").eq(lit(0)).cor(scalarType.testExpr(codeModel, sexp, this.formal));
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return parent.classRef(WrapperRuntime.class).staticInvoke("convertToVector").arg(sexp);
  }

}
