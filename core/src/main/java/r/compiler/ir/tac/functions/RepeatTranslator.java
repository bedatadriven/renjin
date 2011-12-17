package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.statements.GotoStatement;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class RepeatTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("repeat");
  }

  @Override
  public Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    addStatement(builder, context, call);
    return new Constant(Null.INSTANCE);
  }

  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    IRLabel beginLabel = builder.addLabel();
    IRLabel exitLabel = builder.newLabel();
    
    LoopContext loopContext = new LoopContext(beginLabel, exitLabel);
    builder.translateStatements(loopContext, call.getArgument(0));
    
    builder.addStatement(new GotoStatement(beginLabel));
    builder.addLabel(exitLabel);
  }
}
