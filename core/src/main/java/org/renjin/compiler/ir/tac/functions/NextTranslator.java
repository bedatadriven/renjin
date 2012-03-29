package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbol;


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
