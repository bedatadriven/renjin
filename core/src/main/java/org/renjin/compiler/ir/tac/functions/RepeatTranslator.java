package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbol;


public class RepeatTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("repeat");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    addStatement(builder, context, call);
    return new Constant(Null.INSTANCE);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    IRLabel beginLabel = builder.addLabel();
    IRLabel exitLabel = builder.newLabel();
    
    LoopContext loopContext = new LoopContext(beginLabel, exitLabel);
    builder.translateStatements(loopContext, call.getArgument(0));
    
    builder.addStatement(new GotoStatement(beginLabel));
    builder.addLabel(exitLabel);
  }
}
