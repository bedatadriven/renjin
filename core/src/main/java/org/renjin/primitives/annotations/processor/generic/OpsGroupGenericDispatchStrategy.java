package org.renjin.primitives.annotations.processor.generic;

import com.sun.codemodel.*;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
import org.renjin.primitives.annotations.processor.WrapperSourceWriter;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * The 'Ops' group requires special treatment because they are always unary or binary,
 * and dispatch on either the first or second argument, which are always evaluated.
 */
public class OpsGroupGenericDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public OpsGroupGenericDispatchStrategy(JCodeModel codeModel, String name) {
    super(codeModel);
    this.name = name;
  }



  @Override
  public void beforeTypeMatching(WrapperSourceWriter s, int arity) {

    if(arity == 1) {
      s.writeBeginIf("((AbstractSEXP)s0).isObject()");
    } else {
      s.writeBeginIf("((AbstractSEXP)s0).isObject() || ((AbstractSEXP)s1).isObject()");
    }
    
    StringBuilder argsList = new StringBuilder("context, rho, call, \"Ops\", \"" + name + "\", s0");
    if(arity == 2) {
      argsList.append(", s1");
    }

    s.writeStatement("SEXP genericResult = tryDispatchGroupFromPrimitive(" + argsList.toString() + ")");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    
    s.writeCloseBlock();
  }

  @Override
  public void beforeTypeMatching(ApplyMethodContext context,
                                 JExpression functionCall, List<JExpression> arguments,
                                 JBlock parent) {

    JInvocation dispatchInvocation = codeModel.ref(WrapperRuntime.class)
            .staticInvoke("tryDispatchGroupFromPrimitive")
            .arg(context.getContext())
            .arg(context.getEnvironment())
            .arg(functionCall)
            .arg(JExpr.lit("Ops"))
            .arg(JExpr.lit(name));

    for(JExpression arg : arguments) {
      dispatchInvocation.arg(arg);
    }

    JBlock ifObjects = parent._if(anyObjects(arguments))._then();
    JVar dispatchResult = ifObjects.decl(codeModel.ref(SEXP.class), "genericResult", dispatchInvocation);
    ifObjects._if(dispatchResult.ne(JExpr._null()))._then()._return(dispatchResult);
  }

  private JExpression anyObjects(List<JExpression> arguments) {
    if(arguments.size() == 1) {
      return fastIsObject(arguments.get(0));
    } else if(arguments.size() == 2) {
      return fastIsObject(arguments.get(0)).cor(fastIsObject(arguments.get(1)));
    } else {
      throw new UnsupportedOperationException("n arguments = " + arguments.size());
    }
  }
}

