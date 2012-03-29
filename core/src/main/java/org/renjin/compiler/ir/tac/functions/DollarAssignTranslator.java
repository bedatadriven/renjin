package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;


public class DollarAssignTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("$<-");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = call.getArgument(1);
    Expression replacement = builder.translateExpression(context, call.getArgument(2));
    
    return new PrimitiveCall(call, "$<-", builder.simplify(object), new Constant(index), replacement);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    
    // assignment itself has no side effects,
    // evaluate only the rhs for its side effects
    
    builder.addStatement(
        new ExprStatement(builder.translateExpression(context, call.getArgument(2))));
    
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call, Expression rhs) {
    
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = call.getArgument(1);
    
    return new PrimitiveCall(call, "$<-", object, new Constant(index), builder.simplify(rhs));
  
  }

  
}
