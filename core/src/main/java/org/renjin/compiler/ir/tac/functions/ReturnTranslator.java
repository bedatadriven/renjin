package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SexpConstant;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

public class ReturnTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addStatement(builder, context, resolvedFunction, call);
    return SexpConstant.NULL;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    Expression returnExpression;
    if(call.getArguments().length() == 1) {
      returnExpression = builder.translateExpression(context, call.getArgument(0));
    } else {
      returnExpression = SexpConstant.NULL;
    }
    builder.addStatement(new ReturnStatement(returnExpression));
  }
}
