package org.renjin.invoke.codegen.args;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.sexp.ExternalPtr;

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
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexp) {
    JClass externalClass = codeModel.ref(ExternalPtr.class);
    return sexp._instanceof(externalClass)
            .cand(invoke(cast(externalClass, sexp), "getValue")._instanceof(codeModel.ref(formal.getClazz())));
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext method, JExpression sexp) {
    JClass externalClass = method.classRef(ExternalPtr.class);
    JClass formalClass = method.classRef(formal.getClazz());
    return cast(formalClass, invoke(cast(externalClass, sexp), "getValue"));
  }
}
