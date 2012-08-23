package org.renjin.primitives.annotations.processor;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;


public class CodeModelUtils {


  static void returnSexp(JCodeModel codeModel, JBlock parent, JvmMethod overload, JInvocation invocation) {
    if(overload.getReturnType().equals(Void.TYPE)) {
      parent.add(invocation);
      parent._return(codeModel.ref(Null.class).staticRef("INSTANCE"));
    } else {
      parent._return(convertResult(codeModel, overload, invocation));
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
