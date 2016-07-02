package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;

/**
 * Handles a call to UseMethod
 */
public class UseMethodTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, 
                                          TranslationContext context, 
                                          Function resolvedFunction, 
                                          FunctionCall call) {
    
    throw new NotCompilableException(call);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    throw new NotCompilableException(call);
  }
}
