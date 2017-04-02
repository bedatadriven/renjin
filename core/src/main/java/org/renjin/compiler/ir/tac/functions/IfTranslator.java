/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;


public class IfTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
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
      ifFalseResult = Constant.NULL;
    }
    
    builder.addStatement(new Assignment(ifResult, ifFalseResult));
    
    builder.addLabel(endLabel);
    
    return ifResult;
  }

  private boolean hasElse(FunctionCall call) {
    return call.getArguments().length()==3;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {

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
