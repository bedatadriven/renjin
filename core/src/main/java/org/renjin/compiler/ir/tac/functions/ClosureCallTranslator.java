package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.ClosureCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;

import java.util.List;

/**
 * Translates calls to closures.
 */
public class ClosureCallTranslator extends FunctionCallTranslator {
  
  private final Closure function;

  public ClosureCallTranslator(Closure function) {
    this.function = function;
    
  }


  @Override
  public Expression translateToExpression(IRBodyBuilder builder, 
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    
    List<IRArgument> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new ClosureCall(builder.getEvaluationContext(), call, function, arguments);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
