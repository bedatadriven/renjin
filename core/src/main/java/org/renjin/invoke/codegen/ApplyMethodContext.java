package org.renjin.invoke.codegen;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;

public interface ApplyMethodContext {

  JExpression getContext();
  JExpression getEnvironment();

  JClass classRef(Class<?> clazz);

  JCodeModel getCodeModel();
}
