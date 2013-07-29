package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;


public class DollarAssignTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    

    throw new NotCompilableException(call);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    throw new NotCompilableException(call);
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder,
                                                TranslationContext context, Function resolvedFunction, FunctionCall call, Expression rhs) {

    throw new NotCompilableException(call);
  
  }

  
}
