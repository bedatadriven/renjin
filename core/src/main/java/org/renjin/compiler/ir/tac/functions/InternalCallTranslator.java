package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class InternalCallTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get(".Internal");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    SEXP argument = call.getArgument(0);
    if(!(argument instanceof FunctionCall)) {
      throw new InvalidSyntaxException(".Internal() expects a language object as its only argument");
    }
    FunctionCall primitiveCall = (FunctionCall) argument;

    return builder.translatePrimitiveCall(context, primitiveCall);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    builder.addStatement(
        new ExprStatement(translateToExpression(builder, context, call)));
  }
}
