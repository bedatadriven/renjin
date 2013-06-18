package org.renjin.invoke.codegen.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.JvmMethod.Argument;


/**
 * Base class for the different strategies for converting incoming argument (SEXPs) to
 * the types declared in the java method.
 * 
 * @author alex
 *
 */
public abstract class ArgConverterStrategy {

  protected final JvmMethod.Argument formal;
  
  public ArgConverterStrategy(Argument formal) {
    super();
    this.formal = formal;
  }

  public abstract JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable);

  public abstract JExpression convertArgument(ApplyMethodContext method, JExpression sexp);

}
