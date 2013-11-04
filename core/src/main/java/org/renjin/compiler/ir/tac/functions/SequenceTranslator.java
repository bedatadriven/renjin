package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SequenceExpression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;

public class SequenceTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context,
                                          Function resolvedFunction, FunctionCall call) {

    return new SequenceExpression(
      builder.translateSimpleExpression(context, call.getArgument(0)),
      builder.translateSimpleExpression(context, call.getArgument(1)));

  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
