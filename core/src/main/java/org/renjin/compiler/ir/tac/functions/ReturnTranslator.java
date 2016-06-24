package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;

public class ReturnTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addStatement(builder, context, resolvedFunction, call);
    return Constant.NULL;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    Expression returnExpression;
    if(call.getArguments().length() == 1) {
      returnExpression = builder.translateExpression(context, call.getArgument(0));
    } else {
      returnExpression = Constant.NULL;
    }
    builder.addStatement(new ReturnStatement(returnExpression));
  }
}
