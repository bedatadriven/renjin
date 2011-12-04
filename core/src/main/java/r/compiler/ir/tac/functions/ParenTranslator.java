package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class ParenTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("(");
  }

  @Override
  public Operand translateToExpression(TacFactory builder, TranslationContext context, FunctionCall call) {
    return builder.translateExpression(context, call.getArgument(0));
  }

  @Override
  public void addStatement(TacFactory builder, TranslationContext context, FunctionCall call) {
    builder.translateStatements(context, call);
  }
}
