package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.lang.FunctionCall;
import r.lang.Symbol;
import r.lang.Symbols;

public class SwitchTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("switch");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    
    // pass the argument to switch unevaluated, we'll implement
    // tranlsation into IR later when we upgrade to 2.14 base package,
    // in which switch() is not internal.
    
    Expression expr = builder.translateSimpleExpression(context, call.getArgument(0));
    
    return new PrimitiveCall(call, "switch", expr, new EnvironmentVariable(Symbols.ELLIPSES));
            
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    // TODO Auto-generated method stub
    
  }
  

}
