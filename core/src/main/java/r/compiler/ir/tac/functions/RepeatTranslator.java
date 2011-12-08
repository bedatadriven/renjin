package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class RepeatTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("repeat");
  }

  @Override
  public Operand translateToExpression(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    return new Constant(Null.INSTANCE);
  }

  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    IRLabel beginLabel = builder.newLabel();
    IRLabel exitLabel = builder.newLabel();
    
    builder.addLabel(beginLabel);
    
    LoopContext loopContext = new LoopContext(beginLabel, exitLabel);
    builder.translateStatements(loopContext, call.getArgument(0));
    
    builder.addStatement(new GotoStatement(beginLabel));
    builder.addLabel(exitLabel);
   
  }
}
