package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.CmpGE;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.ElementAccess;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Increment;
import org.renjin.compiler.ir.tac.expressions.Length;
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

public class ForTranslator extends FunctionCallTranslator {
  
  @Override
  public Symbol getName() {
    return Symbol.get("for");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
  }
 
  private void addForLoop(IRBodyBuilder factory, TranslationContext context, FunctionCall call) {
    
    Symbol symbol = call.getArgument(0);
    LocalVariable counter = factory.newLocalVariable();
    Temp length = factory.newTemp();
    
    Variable elementVariable = new EnvironmentVariable(symbol);
    
    Expression vector = 
        factory.translateSimpleExpression(context, call.getArgument(1));
    
    SEXP body = call.getArgument(2);

    IRLabel counterLabel = factory.newLabel();
    IRLabel bodyLabel = factory.newLabel();
    IRLabel nextLabel = factory.newLabel();
    IRLabel exitLabel = factory.newLabel();
       
    // initialize the counter
    factory.addStatement(new Assignment(counter, new Constant(0)));
    factory.addStatement(new Assignment(length, 
        new Length((Expression)vector)));

    // check the counter and potentially loop
    factory.addLabel(counterLabel);
    factory.addStatement(new IfStatement(new CmpGE(counter, length), exitLabel, bodyLabel));
    
    // start the body here
    factory.addLabel(bodyLabel);
    factory.addStatement(new Assignment(elementVariable, new ElementAccess(vector, counter)));

    LoopContext loopContext = new LoopContext(nextLabel, exitLabel);
    factory.translateStatements(loopContext, body);
    
    // increment the counter
    factory.addLabel(nextLabel);
    factory.addStatement(new Assignment(counter, new Increment(counter)));
    factory.addStatement(new GotoStatement(counterLabel));

    factory.addLabel(exitLabel);
  }  
}
