package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Symbol;


public class AndTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("&&");
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {
    
    Temp result = builder.newTemp();
    IRLabel firstTrue = builder.newLabel(); /* first is true, need to check second */
    IRLabel firstNA = builder.newLabel(); /* first is NA, need to check second */
    
    IRLabel test2Label = builder.newLabel(); /* conduct second test */

    IRLabel falseLabel = builder.newLabel(); 
    IRLabel naLabel = builder.newLabel();
    IRLabel finishLabel = builder.newLabel();
    
    // check the first condition
    SimpleExpression condition1 = builder.translateSimpleExpression(context, call.getArgument(0));
    builder.addStatement(new IfStatement(condition1, firstTrue, falseLabel, firstNA));
    
    // first is true.
    // set the result to true and do the next test
    builder.addLabel(firstTrue);
    builder.addStatement(new Assignment(result, new Constant(new LogicalArrayVector(true))));
    builder.addStatement(new GotoStatement(test2Label));
    
    // first is NA
    // set the result to NA and do the next test
    builder.addLabel(firstNA);
    builder.addStatement(new Assignment(result, new Constant(new LogicalArrayVector(LogicalVector.NA))));
    builder.addStatement(new GotoStatement(test2Label));
    
    // check second condition
    builder.addLabel(test2Label);
    SimpleExpression condition2 = builder.translateSimpleExpression(context, call.getArgument(1));
    builder.addStatement(new IfStatement(condition2, 
        finishLabel,  // if condition 2 is true, then the result is equal to condition2
        falseLabel, // if false, final result is false,
        naLabel));

    builder.addLabel(falseLabel);
    builder.addStatement(new Assignment(result, new Constant(new LogicalArrayVector(false))));
    builder.addStatement(new GotoStatement(finishLabel));
    
    builder.addLabel(naLabel);
    builder.addStatement(new Assignment(result, new Constant(new LogicalArrayVector(LogicalVector.NA))));
    
    builder.addLabel(finishLabel);
   
    return result;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
      FunctionCall call) {
    
    IRLabel test2Label = builder.newLabel();
    IRLabel finishLabel = builder.newLabel();
    
    // check the first condition
    SimpleExpression condition1 = builder.translateSimpleExpression(context, call.getArgument(0));
    builder.addStatement(new IfStatement(condition1, test2Label, finishLabel, test2Label));
    
    // first condition is ok, check the second
    builder.addLabel(test2Label);
    builder.addStatement(new ExprStatement(builder.translateExpression(context, call.getArgument(1))));
    
    builder.addLabel(finishLabel);
   
  }

}
