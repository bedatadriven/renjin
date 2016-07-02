package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;


public abstract class FunctionCallTranslator {

  /**
   * Translates a function call S-Expression to an IR expression
   */
  public abstract Expression translateToExpression(IRBodyBuilder builder, TranslationContext context,
                                                   Function resolvedFunction, FunctionCall call);


  public Expression translateToSetterExpression(IRBodyBuilder builder,
                                                TranslationContext context,
                                                Function resolvedFunction,
                                                FunctionCall getterCall, Expression rhs) {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " is not a setter");
  }
  
  public abstract void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call);
 
 
}
