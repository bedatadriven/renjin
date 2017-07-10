package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.NamedSlotAccess;
import org.renjin.compiler.ir.tac.expressions.ReplaceSlotExpression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class SlotAssignTranslator extends FunctionCallTranslator {
  
  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {
    
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
  
  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                Function resolvedFunction, FunctionCall getterCall, Expression rhs) {
  
    Expression object = builder.translateExpression(context, getterCall.getArgument(0));
    String name = parseName(getterCall);
  
    return new ReplaceSlotExpression(object, rhs, Symbol.get(name));
    
  }
  
  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));
    String name = parseName(call);
    
    return new NamedSlotAccess(object, name);
  }
  
  public String parseName(FunctionCall call) {
    SEXP nameArgument = call.getArgument(1);
    if(!(nameArgument instanceof Symbol)) {
      throw new NotCompilableException(call);
    }
    return ((Symbol) nameArgument).getPrintName();
  }
}
