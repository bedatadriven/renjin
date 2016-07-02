package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;


public class ParenTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    return builder.translateExpression(context, call.getArgument(0));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.translateStatements(context, call);
  }
}
