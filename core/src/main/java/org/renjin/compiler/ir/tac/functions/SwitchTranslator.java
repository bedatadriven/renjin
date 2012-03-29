package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;


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
