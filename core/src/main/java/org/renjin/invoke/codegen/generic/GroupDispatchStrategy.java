package org.renjin.invoke.codegen.generic;

import com.sun.codemodel.*;
import org.renjin.invoke.codegen.ApplyMethodContext;
import org.renjin.invoke.codegen.VarArgParser;
import org.renjin.primitives.S3;
import org.renjin.sexp.SEXP;

import java.util.List;

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr.lit;

/**
 * Dispatches a S3 generic 
 */
public class GroupDispatchStrategy extends GenericDispatchStrategy {

  private String groupName;
  private final String methodName;

  public GroupDispatchStrategy(JCodeModel codeModel, String groupName, String methodName) {
    super(codeModel);
    this.groupName = groupName;
    this.methodName = methodName;
  }

  @Override
  public void afterFirstArgIsEvaluated(ApplyMethodContext context, JExpression functionCall, JExpression arguments,
                                       JBlock parent, JExpression argument) {

    JBlock ifObject = parent._if(fastIsObject(argument))._then();
    JExpression genericResult = ifObject.decl(codeModel.ref(SEXP.class), "genericResult",
        codeModel.ref(S3.class).staticInvoke("tryDispatchGroupFromPrimitive")
            .arg(context.getContext())
            .arg(context.getEnvironment())
            .arg(functionCall)
            .arg(lit(groupName))
            .arg(lit(methodName))
            .arg(argument)
            .arg(arguments));
    ifObject._if(genericResult.ne(_null()))._then()._return(genericResult);
  }


}
