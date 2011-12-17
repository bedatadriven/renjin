package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.expressions.Expression;
import r.lang.FunctionCall;
import r.lang.Symbol;

public abstract class FunctionCallTranslator {

  public abstract Symbol getName();
  
  public abstract Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, 
      FunctionCall call);

  public abstract void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call);
 
 
}
