package org.renjin.primitives.annotations.processor.generic;

import com.sun.codemodel.*;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.ArgumentItType;
import org.renjin.primitives.annotations.processor.VarArgParser;
import org.renjin.primitives.annotations.processor.WrapperSourceWriter;
import org.renjin.sexp.AbstractSEXP;

import java.util.List;

import static com.sun.codemodel.JExpr.cast;


public class GenericDispatchStrategy {

  protected final JCodeModel codeModel;

  public GenericDispatchStrategy(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public void afterArgIsEvaluated(WrapperSourceWriter s, int index, ArgumentItType argItType) {

  }

  public void afterArgIsEvaluated(ApplyMethodContext context, JExpression functionCall, JExpression arguments,
                                  JBlock parent, JExpression argument, int index) {

  }

  public void beforeTypeMatching(WrapperSourceWriter s, int arity) {

  }

  public void beforePrimitiveCalled(WrapperSourceWriter s) {
    
  }

  public void beforeTypeMatching(ApplyMethodContext context, JExpression functionCall,
                                 List<JExpression> arguments, JBlock parent) {


  }

  protected JInvocation fastIsObject(JExpression argument) {
    // without the explicit cast to AbstractSEXP, the JVM will not inline the call to isObject
    // which has a drastic impact on performance
    return JExpr.invoke(cast(codeModel.ref(AbstractSEXP.class), argument), "isObject");
  }

  public void beforePrimitiveCalled(JBlock parent, VarArgParser args, ApplyMethodContext context, JExpression call) {

  }
}
