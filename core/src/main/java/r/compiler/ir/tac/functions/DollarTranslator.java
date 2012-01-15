package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.statements.ExprStatement;
import r.lang.FunctionCall;
import r.lang.Symbol;

public class DollarTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("$");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));
    Symbol index = call.getArgument(1);
    
    return new PrimitiveCall("$", builder.simplify(object), new Constant(index) ); 
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    
    // TODO: does x$a ever have any side effects? Maybe forces a promise?
    // if not, we can do a NO OP here.
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
    
  }
}
