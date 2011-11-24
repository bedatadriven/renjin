package r.compiler.ir.tac.functions;

import com.google.common.collect.Lists;

import r.compiler.ir.tac.ElementAccess;
import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.instructions.ConditionalJump;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.IncrementCounter;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.DynamicCall;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.CmpGE;
import r.compiler.ir.tac.operand.Temp;
import r.compiler.ir.tac.operand.Variable;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbol;

public class ForTranslator extends FunctionCallTranslator {
  
  @Override
  public Symbol getName() {
    return Symbol.get("for");
  }

  @Override
  public Operand translateToRValue(TacFactory factory, FunctionCall call) {
    addForLoop(factory, call);
    
    return new Constant(Null.INSTANCE);
  }


  @Override
  public void addStatement(TacFactory factory, FunctionCall call) {
    addForLoop(factory, call);
  }
 
  private void addForLoop(TacFactory factory, FunctionCall call) {
    Symbol symbol = call.getArgument(0);
    Temp counter = factory.newTemp();
    Temp length = factory.newTemp();
    
    Variable elementVariable = new Variable(symbol);
    
    Operand vector = 
        factory.simplify(
            factory.translateToRValue(call.getArgument(1)));
    
    SEXP body = call.getArgument(2);

    Label bodyLabel = factory.newLabel();
    Label counterLabel = factory.newLabel();
     
    // initialize the counter
    factory.addAssignment(counter, new Constant(0));
    factory.addAssignment(length, new DynamicCall(Symbol.get("length"), Lists.newArrayList((Operand)elementVariable)));
    factory.addNode(new GotoStatement(counterLabel));
    
    // start the body here
    factory.addNode(bodyLabel);
    factory.addAssignment(elementVariable, new ElementAccess(vector, counter));
    factory.addStatement(body);
    
    // increment the counter
    factory.addNode(new IncrementCounter(counter));
    
    // check the counter and potentially loop
    factory.addNode(counterLabel);
    factory.addNode(new ConditionalJump(new CmpGE(counter, length), bodyLabel));
    
  }
}
