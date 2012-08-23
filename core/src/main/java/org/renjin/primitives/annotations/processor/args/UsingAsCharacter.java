package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.InvokeAsCharacter;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.primitives.annotations.processor.WrapperRuntime;


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
