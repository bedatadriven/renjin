package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.TacFactory;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.ConditionalJump;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Temp;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Symbol;

public class IfTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("if");
  }

  @Override
  public Operand translateToRValue(TacFactory factory, FunctionCall call) {
    Operand condition = factory.translateToRValue(call.getArgument(0));
    
    // since "if" is being used in the context of an expression, we need
    // to store its final value somewhere
    Temp ifResult = factory.newTemp(); 
    
    Label ifFalseLabel = factory.newLabel();
    Label endLabel = factory.newLabel();
    
    ConditionalJump jump = new ConditionalJump(factory.simplify(condition), ifFalseLabel);
    factory.addNode(jump);
    
    // evaluate "if true" expression
    Operand ifTrueResult = factory.translateToRValue(call.getArgument(1));
    
    // assign this result to our temp value
    factory.addNode(new Assignment(ifResult, ifTrueResult));

    factory.addNode(new GotoStatement(endLabel));
    
    factory.addNode(ifFalseLabel);
    
    // next evaluate "if false" expression
    // if the false clause is absent, it evaluates to 
    // NULL
    Operand ifFalseResult;
    if(hasElse(call)) {
      ifFalseResult = factory.simplify(factory.translateToRValue(
          call.getArgument(2)));
    } else {
      ifFalseResult = new Constant(Null.INSTANCE);
    }
    
    factory.addAssignment(ifResult, ifFalseResult);
    
    factory.addNode(endLabel);
    
    return ifResult;
  }

  private boolean hasElse(FunctionCall call) {
    return call.getArguments().length()==3;
  }

  @Override
  public void addStatement(TacFactory factory, FunctionCall call) {

    Operand condition = factory.translateToRValue(call.getArgument(0));
    Label endLabel = factory.newLabel();
    Label ifFalseLabel;
    if(hasElse(call)) {
      ifFalseLabel = factory.newLabel();
    } else {
      ifFalseLabel = endLabel;
    }
    
    ConditionalJump jump = new ConditionalJump(factory.simplify(condition), ifFalseLabel);
    factory.addNode(jump);
    
    // evaluate "if true" expression for side effects
    factory.addStatement(call.getArgument(1));
    
    factory.addNode(new GotoStatement(endLabel));
    
    if(hasElse(call)) {
      factory.addNode(ifFalseLabel);
      factory.addStatement(call.getArgument(2));
    }    
    factory.addNode(endLabel);
  }
}
