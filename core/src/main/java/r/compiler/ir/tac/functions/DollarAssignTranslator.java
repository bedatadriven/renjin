package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.statements.ExprStatement;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class DollarAssignTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("$<-");
  }

  @Override
  public Expression translateToExpression(IRScopeBuilder builder,
      TranslationContext context, FunctionCall call) {
    
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = call.getArgument(1);
    Expression replacement = builder.translateExpression(context, call.getArgument(2));
    
    return new PrimitiveCall("$<-", builder.simplify(object), new Constant(index), replacement);
  }

  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context,
      FunctionCall call) {
    
    // assignment itself has no side effects,
    // evaluate only the rhs for its side effects
    
    builder.addStatement(
        new ExprStatement(builder.translateExpression(context, call.getArgument(2))));
    
  }

  @Override
  public Expression translateToSetterExpression(IRScopeBuilder builder,
      TranslationContext context, FunctionCall call, Expression rhs) {
    
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = call.getArgument(1);
    
    return new PrimitiveCall("$<-", object, new Constant(index), rhs);
  
  }

  
}
