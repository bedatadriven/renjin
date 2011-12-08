package r.compiler.ir.tac.functions;

import r.compiler.ir.exception.InvalidSyntaxException;
import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class NextTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("next");
  }

  @Override
  public Operand translateToExpression(IRBlockBuilder builder,
      TranslationContext context, FunctionCall call) {

    addStatement(builder, context, call);
    return new Constant(Null.INSTANCE); 
  }

  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context,
      FunctionCall call) {

    if(!(context instanceof LoopContext)) {
      throw new InvalidSyntaxException("`next` cannot be used outside of a loop");
    }
    LoopContext loopContext = (LoopContext)context;
    builder.addStatement( new GotoStatement(loopContext.getStartLabel()));
  }
}
