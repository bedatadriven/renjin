package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.CmpGE;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.ElementAccess;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Increment;
import org.renjin.compiler.ir.tac.expressions.LocalVariable;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


import com.google.common.collect.Lists;

public class WhileTranslator extends FunctionCallTranslator {

  
  @Override
  public Symbol getName() {
    return Symbol.get("for");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    addLoop(builder, context, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
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
