package org.renjin.invoke.codegen;

import com.sun.codemodel.*;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


public class CodeModelUtils {


  static void returnSexp(JVar context, JCodeModel codeModel, JBlock parent, JvmMethod overload, JInvocation invocation) {
    
    if(overload.getReturnType().equals(Void.TYPE)) {
      parent.add(invocation);
      parent.add(context.invoke("setInvisibleFlag"));
      parent._return(codeModel.ref(Null.class).staticRef("INSTANCE"));
    } else {
      JVar result = parent.decl(codeModel._ref(SEXP.class), "__return", convertResult(codeModel, overload, invocation));

      if(overload.isInvisible()) {
        parent.add(context.invoke("setInvisibleFlag"));
      }
      parent._return(result);
    }
  }

  static JExpression convertResult(JCodeModel codeModel, JvmMethod overload, JInvocation invocation) {
    if(SEXP.class.isAssignableFrom(overload.getReturnType())) {
      return invocation;
    } else {
      return codeModel.ref(WrapperRuntime.class).staticInvoke("wrapResult").arg(invocation);
    }
  }

}
