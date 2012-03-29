package org.renjin.compiler.ir.tac.functions;


import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbol;


public class IfTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("if");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    SimpleExpression condition = builder.translateSimpleExpression(context, call.getArgument(0));
    
    // since "if" is being used in the context of an expression, we need
    // to store its final value somewhere
    Temp ifResult = builder.newTemp(); 
    
    IRLabel trueTarget = builder.newLabel();
    IRLabel falseTarget = builder.newLabel();
    IRLabel endLabel = builder.newLabel();
    
    IfStatement jump = new IfStatement(condition, trueTarget, falseTarget);
    builder.addStatement(jump);
    
    // evaluate "if true" expression
    builder.addLabel(trueTarget);
    Expression ifTrueResult = builder.translateExpression(context, call.getArgument(1));
    
    // assign this result to our temp value
    builder.addStatement(new Assignment(ifResult, ifTrueResult));

    builder.addStatement(new GotoStatement(endLabel));
    
    builder.addLabel(falseTarget);
    
    // next evaluate "if false" expression
    // if the false clause is absent, it evaluates to 
    // NULL
    Expression ifFalseResult;
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
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {

    SimpleExpression condition = builder.translateSimpleExpression(context, call.getArgument(0));
    IRLabel trueLabel = builder.newLabel();
    IRLabel falseLabel = builder.newLabel();
    IRLabel endLabel;

    if(hasElse(call)) {
      endLabel = builder.newLabel();
    } else {
      endLabel = falseLabel;
    }
    
    IfStatement jump = new IfStatement(condition, trueLabel, falseLabel);
    builder.addStatement(jump);
    
    // evaluate "if true" expression for side effects
    builder.addLabel(trueLabel);
    builder.translateStatements(context, call.getArgument(1));
    
    if(hasElse(call)) {
      builder.addStatement(new GotoStatement(endLabel));
      builder.addLabel(falseLabel);
      builder.translateStatements(context, call.getArgument(2));
    }    
    
    builder.addLabel(endLabel);
  }
}
