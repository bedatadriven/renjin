package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SexpConstant;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

public class WhileTranslator extends FunctionCallTranslator {


  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addLoop(builder, context, call);
    
    return SexpConstant.NULL;
  }


  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addLoop(builder, context, call);
  }
 
  private void addLoop(IRBodyBuilder factory, TranslationContext context, FunctionCall call) {
    
    SEXP condition = call.getArgument(0);
    
    SEXP body = call.getArgument(1);

    IRLabel checkLabel = factory.newLabel();
    IRLabel bodyLabel = factory.newLabel();
    IRLabel exitLabel = factory.newLabel();
       
    // check the counter and potentially loop
    factory.addLabel(checkLabel);
    factory.addStatement(
        new IfStatement(factory.translateSimpleExpression(context, condition),
            bodyLabel, exitLabel));
    
    // start the body here
    factory.addLabel(bodyLabel);

    LoopContext loopContext = new LoopContext(checkLabel, exitLabel);
    factory.translateStatements(loopContext, body);
    
    // increment the counter
    factory.addStatement(new GotoStatement(checkLabel));
    
    factory.addLabel(exitLabel);
  }
  
}
