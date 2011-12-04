package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.ConditionalJump;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.SimpleExpr;
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
  public Operand translateToExpression(TranslationContext context, FunctionCall call) {
    SimpleExpr condition = context.translateSimpleExpression(call.getArgument(0));
    
    // since "if" is being used in the context of an expression, we need
    // to store its final value somewhere
    Temp ifResult = context.newTemp(); 
    
    Label ifFalseLabel = context.newLabel();
    Label endLabel = context.newLabel();
    
    ConditionalJump jump = new ConditionalJump(condition, ifFalseLabel);
    context.addStatement(jump);
    
    // evaluate "if true" expression
    Operand ifTrueResult = context.translateExpression(call.getArgument(1));
    
    // assign this result to our temp value
    context.addStatement(new Assignment(ifResult, ifTrueResult));

    context.addStatement(new GotoStatement(endLabel));
    
    context.addLabel(ifFalseLabel);
    
    // next evaluate "if false" expression
    // if the false clause is absent, it evaluates to 
    // NULL
    Operand ifFalseResult;
    if(hasElse(call)) {
      ifFalseResult = context.translateSimpleExpression(call.getArgument(2));
    } else {
      ifFalseResult = new Constant(Null.INSTANCE);
    }
    
    context.addStatement(new Assignment(ifResult, ifFalseResult));
    
    context.addLabel(endLabel);
    
    return ifResult;
  }

  private boolean hasElse(FunctionCall call) {
    return call.getArguments().length()==3;
  }

  @Override
  public void addStatement(TranslationContext factory, FunctionCall call) {

    SimpleExpr condition = factory.translateSimpleExpression(call.getArgument(0));
    Label endLabel = factory.newLabel();
    Label ifFalseLabel;
    if(hasElse(call)) {
      ifFalseLabel = factory.newLabel();
    } else {
      ifFalseLabel = endLabel;
    }
    
    ConditionalJump jump = new ConditionalJump(condition, ifFalseLabel);
    factory.addStatement(jump);
    
    // evaluate "if true" expression for side effects
    factory.translateStatements(call.getArgument(1));
    
    factory.addStatement(new GotoStatement(endLabel));
    
    if(hasElse(call)) {
      factory.addLabel(ifFalseLabel);
      factory.translateStatements(call.getArgument(2));
    }    
    factory.addLabel(endLabel);
  }
}
