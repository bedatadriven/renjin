package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Expression;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class ParenTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("(");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    return builder.translateExpression(context, call.getArgument(0));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    builder.translateStatements(context, call);
  }
}
