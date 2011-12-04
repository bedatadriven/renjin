package r.compiler.ir.tac.functions;


import r.compiler.ir.tac.Label;
import r.compiler.ir.tac.TacFactory;
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
  public Operand translateToExpression(TacFactory builder, TranslationContext context, FunctionCall call) {
    SimpleExpr condition = builder.translateSimpleExpression(context, call.getArgument(0));
    
    // since "if" is being used in the context of an expression, we need
    // to store its final value somewhere
    Temp ifResult = builder.newTemp(); 
    
    Label ifFalseLabel = builder.newLabel();
    Label endLabel = builder.newLabel();
    
    ConditionalJump jump = new ConditionalJump(condition, ifFalseLabel);
    builder.addStatement(jump);
    
    // evaluate "if true" expression
    Operand ifTrueResult = builder.translateExpression(context, call.getArgument(1));
    
    // assign this result to our temp value
    builder.addStatement(new Assignment(ifResult, ifTrueResult));

    builder.addStatement(new GotoStatement(endLabel));
    
    builder.addLabel(ifFalseLabel);
    
    // next evaluate "if false" expression
    // if the false clause is absent, it evaluates to 
    // NULL
    Operand ifFalseResult;
    if(hasElse(call)) {
      ifFalseResult = builder.translateSimpleExpression(context, call.getArgument(2));
    } else {
      ifFalseResult = new Constant(Null.INSTANCE);
    }
    
    builder.addStatement(new Assignment(ifResult, ifFalseResult));
    
    builder.addLabel(endLabel);
    
    return ifResult;
  }

  private boolean hasElse(FunctionCall call) {
    return call.getArguments().length()==3;
  }

  @Override
  public void addStatement(TacFactory builder, TranslationContext context, FunctionCall call) {

    SimpleExpr condition = builder.translateSimpleExpression(context, call.getArgument(0));
    Label endLabel = builder.newLabel();
    Label ifFalseLabel;
    if(hasElse(call)) {
      ifFalseLabel = builder.newLabel();
    } else {
      ifFalseLabel = endLabel;
    }
    
    ConditionalJump jump = new ConditionalJump(condition, ifFalseLabel);
    builder.addStatement(jump);
    
    // evaluate "if true" expression for side effects
    builder.translateStatements(context, call.getArgument(1));
    
    builder.addStatement(new GotoStatement(endLabel));
    
    if(hasElse(call)) {
      builder.addLabel(ifFalseLabel);
      builder.translateStatements(context, call.getArgument(2));
    }    
    builder.addLabel(endLabel);
  }
}
