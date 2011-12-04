package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class RepeatTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("repeat");
  }

  @Override
  public Operand translateToExpression(TranslationContext context, FunctionCall call) {
    return new Constant(Null.INSTANCE);
  }

  @Override
  public void addStatement(TranslationContext factory, FunctionCall call) {
    Label beginLabel = factory.newLabel();
    factory.addLabel(beginLabel);
    factory.translateStatements(call.getArgument(0));
    factory.addStatement(new GotoStatement(beginLabel));
  }
}
