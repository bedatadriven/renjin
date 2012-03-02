package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Expression;
import r.lang.FunctionCall;
import r.lang.Symbol;

public abstract class FunctionCallTranslator {

  public abstract Symbol getName();
  
  public abstract Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, 
      FunctionCall call);

  public Expression translateToSetterExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call, Expression rhs) {
    throw new UnsupportedOperationException(getName() + " is not a setter");
  }
  
  public abstract void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call);
 
 
}
