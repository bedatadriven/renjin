package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;


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
