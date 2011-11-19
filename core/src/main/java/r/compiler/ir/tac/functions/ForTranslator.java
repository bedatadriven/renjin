package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.AddOp;
import r.compiler.ir.tac.ConditionalJump;
import r.compiler.ir.tac.Constant;
import r.compiler.ir.tac.ElementAccess;
import r.compiler.ir.tac.Expr;
import r.compiler.ir.tac.GotoStatement;
import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.Temp;
import r.compiler.ir.tac.Variable;
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
  public Expr translateToRValue(TacFactory factory, FunctionCall call) {
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
    Variable elementVariable = new Variable(symbol);
    
    Expr vector = 
        factory.simplify(
            factory.translateToRValue(call.getArgument(1)));
    
    SEXP body = call.getArgument(2);

    Label bodyLabel = factory.newLabel();
    Label counterLabel = factory.newLabel();
     
    // initialize the counter
    factory.addAssignment(counter, new Constant(0));
    factory.addNode(new GotoStatement(counterLabel));
    
    // start the body here
    factory.addNode(bodyLabel);
    factory.addAssignment(elementVariable, new ElementAccess(vector, counter));
    factory.addStatement(body);
    
    // increment the counter
    factory.addAssignment(counter, new AddOp(counter, new Constant(1)));
    
    // check the counter and potentially loop
    factory.addNode(counterLabel);
    factory.addNode(new GotoStatement(bodyLabel));
    
    
  }
}
