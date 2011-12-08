package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class ParenTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("(");
  }

  @Override
  public Operand translateToExpression(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    return builder.translateExpression(context, call.getArgument(0));
  }

  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    builder.translateStatements(context, call);
  }
}
