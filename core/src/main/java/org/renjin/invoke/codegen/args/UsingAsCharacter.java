package org.renjin.invoke.codegen.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.annotations.InvokeAsCharacter;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.model.JvmMethod.Argument;
import org.renjin.invoke.codegen.WrapperRuntime;


public class UsingAsCharacter extends ArgConverterStrategy {

  public UsingAsCharacter(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return formal.isAnnotatedWith(InvokeAsCharacter.class);
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return parent.classRef(WrapperRuntime.class).staticInvoke("invokeAsCharacter")
            .arg(parent.getContext())
            .arg(parent.getEnvironment())
            .arg(sexp);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return JExpr.TRUE;
  }
}
