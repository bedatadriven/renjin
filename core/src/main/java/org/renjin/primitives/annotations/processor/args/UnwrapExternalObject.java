package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.sexp.ExternalExp;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;


public class UnwrapExternalObject extends ArgConverterStrategy {

  public UnwrapExternalObject(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return !formal.getClazz().isPrimitive();
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return "WrapperRuntime.<" + formal.getClazz().getName() + ">unwrapExternal(" + argumentExpression + ")";
  }

  @Override
  public String getTestExpr(String argLocal) {
    return argLocal + " instanceof " + ExternalExp.class.getSimpleName() + " && " +
        "((" + ExternalExp.class.getSimpleName() + ")" + argLocal + ").getValue() instanceof " + formal.getClazz().getName();
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexp) {
    JClass externalClass = codeModel.ref(ExternalExp.class);
    return sexp._instanceof(externalClass)
            .cand(invoke(cast(externalClass, sexp), "getValue")._instanceof(codeModel.ref(formal.getClazz())));
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext method, JExpression sexp) {
    JClass externalClass = method.classRef(ExternalExp.class);
    JClass formalClass = method.classRef(formal.getClazz());
    return cast(formalClass, invoke(cast(externalClass, sexp), "getValue"));
  }
}
