package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.IfStatement;
import r.compiler.ir.tac.instructions.IncrementCounter;
import r.compiler.ir.tac.operand.CmpGE;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.ElementAccess;
import r.compiler.ir.tac.operand.EnvironmentVariable;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.PrimitiveCall;
import r.compiler.ir.tac.operand.TempVariable;
import r.compiler.ir.tac.operand.Variable;
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
  public Operand translateToExpression(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(IRBlockBuilder builder, TranslationContext context, FunctionCall call) {
    addForLoop(builder, context, call);
  }
 
  private void addForLoop(IRBlockBuilder factory, TranslationContext context, FunctionCall call) {
    
    Symbol symbol = call.getArgument(0);
    TempVariable counter = factory.newTemp();
    TempVariable length = factory.newTemp();
    
    Variable elementVariable = new EnvironmentVariable(symbol);
    
    Operand vector = 
        factory.translateSimpleExpression(context, call.getArgument(1));
    
    SEXP body = call.getArgument(2);

    IRLabel counterLabel = factory.newLabel();
    IRLabel bodyLabel = factory.newLabel();
    IRLabel nextLabel = factory.newLabel();
    IRLabel exitLabel = factory.newLabel();
       
    // initialize the counter
    factory.addStatement(new Assignment(counter, new Constant(0)));
    factory.addStatement(new Assignment(length, 
        new PrimitiveCall(Symbol.get("length"), Lists.newArrayList((Operand)vector))));

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
    factory.addStatement(new IncrementCounter(counter));
    factory.addStatement(new GotoStatement(counterLabel));

    factory.addLabel(exitLabel);

  }  
}
