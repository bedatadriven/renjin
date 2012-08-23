package org.renjin.primitives.annotations.processor.generic;

import com.sun.codemodel.*;
import org.renjin.primitives.annotations.processor.ApplyMethodContext;
import org.renjin.primitives.annotations.processor.VarArgParser;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
import org.renjin.primitives.annotations.processor.WrapperSourceWriter;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr.lit;

public class SummaryGroupGenericStrategy extends GenericDispatchStrategy {

  private final String name;

  public SummaryGroupGenericStrategy(JCodeModel codeModel, String name) {
    super(codeModel);
    this.name = name;
  }

  
  @Override
  public void beforePrimitiveCalled(WrapperSourceWriter s) {

    s.writeBeginIf("argList.length() > 0 && ((AbstractSEXP)argList.getElementAsSEXP(0)).isObject()");
    

    s.writeStatement("SEXP genericResult = tryDispatchSummaryFromPrimitive(context, rho, call, " +
    		"\"" + name + "\", argList, arg0)");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    
    s.writeCloseBlock();
  }

  @Override
  public void beforePrimitiveCalled(JBlock parent, VarArgParser args, ApplyMethodContext context, JExpression call) {
    JBlock isObject = parent._if(args.getVarArgBuilder().invoke("length").gt(lit(0))
            .cand(fastIsObject(args.getVarArgList().invoke("getElementAsSEXP").arg(lit(0)))))._then();
    JVar genericResult = isObject.decl(codeModel.ref(SEXP.class), "genericResult",
            codeModel.ref(WrapperRuntime.class)
                    .staticInvoke("tryDispatchSummaryFromPrimitive")
                    .arg(context.getContext())
                    .arg(context.getEnvironment())
                    .arg(call)
                    .arg(lit(name))
                    .arg(args.getVarArgList())
                    .arg(args.getNamedFlagJExp("na.rm")));
    isObject._if(genericResult.ne(JExpr._null()))
            ._then()
            ._return(genericResult);
  }



}
