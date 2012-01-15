package r.compiler.ir.tac.functions;

import r.compiler.ir.exception.InvalidSyntaxException;
import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.statements.GotoStatement;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class NextTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("next");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {

    addStatement(builder, context, call);
    return new Constant(Null.INSTANCE); 
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {

    if(!(context instanceof LoopContext)) {
      throw new InvalidSyntaxException("`next` cannot be used outside of a loop");
    }
    LoopContext loopContext = (LoopContext)context;
    builder.addStatement( new GotoStatement(loopContext.getStartLabel()));
  }
}
