package org.renjin.primitives.annotations.processor.args;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.JvmMethod.Argument;
import org.renjin.sexp.SEXP;


/**
 * Handles checked narrowing of types to declared subclass of SEXP
 * @author alex
 *
 */
public class SexpSubclass extends ArgConverterStrategy {

  public SexpSubclass(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return SEXP.class.isAssignableFrom(formal.getClazz());
  }

  @Override
  public JExpression convertArgument(ApplyMethodContext parent, JExpression sexp) {
    return JExpr.cast(parent.classRef(formal.getClazz()), sexp);
  }

  @Override
  public JExpression getTestExpr(JCodeModel codeModel, JVar sexpVariable) {
    return sexpVariable._instanceof(codeModel._ref(formal.getClazz()));
  }
}
