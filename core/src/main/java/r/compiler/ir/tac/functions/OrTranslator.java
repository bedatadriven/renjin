package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.IRBodyBuilder;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.SimpleExpression;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.ExprStatement;
import r.compiler.ir.tac.statements.GotoStatement;
import r.compiler.ir.tac.statements.IfStatement;
import r.lang.FunctionCall;
import r.lang.LogicalVector;
import r.lang.Symbol;

public class OrTranslator extends FunctionCallTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("||");
  }
  

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
      TranslationContext context, FunctionCall call) {

    Temp result = builder.newTemp();
    IRLabel firstFalse = builder.newLabel(); /* first is false, need to check second */
    IRLabel firstNA = builder.newLabel(); /* first is NA, need to check second */
    
    IRLabel test2Label = builder.newLabel(); /* conduct second test */

    IRLabel trueLabel = builder.newLabel(); 
    IRLabel naLabel = builder.newLabel();
    IRLabel finishLabel = builder.newLabel();
    
    // check the first condition
    SimpleExpression condition1 = builder.translateSimpleExpression(context, call.getArgument(0));
    builder.addStatement(new IfStatement(condition1, trueLabel, firstFalse, firstNA));
    
    // first is true.
    // set the result to true and do the next test
    builder.addLabel(firstFalse);
    builder.addStatement(new Assignment(result, new Constant(new LogicalVector(false))));
    builder.addStatement(new GotoStatement(test2Label));
    
    // first is NA
    // set the result to NA and do the next test
    builder.addLabel(firstNA);
    builder.addStatement(new Assignment(result, new Constant(new LogicalVector(LogicalVector.NA))));
    builder.addStatement(new GotoStatement(test2Label));
    
    // check second condition
    builder.addLabel(test2Label);
    SimpleExpression condition2 = builder.translateSimpleExpression(context, call.getArgument(1));
    builder.addStatement(new IfStatement(condition2, 
        trueLabel, 
        finishLabel,  // if condition 2 is false, then the result is equal to condition1
        naLabel));

    builder.addLabel(trueLabel);
    builder.addStatement(new Assignment(result, new Constant(new LogicalVector(true))));
    builder.addStatement(new GotoStatement(finishLabel));
    
    builder.addLabel(naLabel);
    builder.addStatement(new Assignment(result, new Constant(new LogicalVector(LogicalVector.NA))));
    
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
    builder.addStatement(new IfStatement(condition1, finishLabel, test2Label, test2Label));
    
    // first condition is ok, check the second
    builder.addLabel(test2Label);
    builder.addStatement(new ExprStatement(builder.translateExpression(context, call.getArgument(1))));
    
    builder.addLabel(finishLabel);
   
  }
}
