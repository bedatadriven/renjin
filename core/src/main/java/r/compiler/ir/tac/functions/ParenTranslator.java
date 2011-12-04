package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class ParenTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("(");
  }

  @Override
  public Operand translateToExpression(TranslationContext context, FunctionCall call) {
    return context.translateExpression(call.getArgument(0));
  }

  @Override
  public void addStatement(TranslationContext factory, FunctionCall call) {
    factory.translateStatements(call);
  }
}
