package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.CmpGE;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.Increment;
import r.compiler.ir.tac.expressions.LocalVariable;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.expressions.Variable;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.GotoStatement;
import r.compiler.ir.tac.statements.IfStatement;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbol;

import com.google.common.collect.Lists;

public class ForTranslator extends FunctionCallTranslator {
  
  @Override
  public Symbol getName() {
    return Symbol.get("for");
  }

  @Override
  public Expression translateToExpression(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(IRScopeBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
  }
 
  private void addForLoop(IRScopeBuilder factory, TranslationContext context, FunctionCall call) {
    
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
        new PrimitiveCall(Symbol.get("length"), Lists.newArrayList((Expression)vector))));

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
