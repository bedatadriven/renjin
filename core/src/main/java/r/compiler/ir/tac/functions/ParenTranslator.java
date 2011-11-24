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
  public Operand translateToRValue(TacFactory factory, FunctionCall call) {
    return factory.translateToRValue(call.getArgument(0));
  }

  @Override
  public void addStatement(TacFactory factory, FunctionCall exp) {
    factory.addStatement(exp);
  }
}
