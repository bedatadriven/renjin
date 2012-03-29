package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;


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
