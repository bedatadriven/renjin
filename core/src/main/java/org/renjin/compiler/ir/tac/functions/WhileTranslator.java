/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

public class WhileTranslator extends FunctionCallTranslator {


  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addLoop(builder, context, call);
    
    return Constant.NULL;
  }


  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addLoop(builder, context, call);
  }
 
  private void addLoop(IRBodyBuilder factory, TranslationContext context, FunctionCall call) {
    
    SEXP condition = call.getArgument(0);
    
    SEXP body = call.getArgument(1);

    IRLabel checkLabel = factory.newLabel();
    IRLabel bodyLabel = factory.newLabel();
    IRLabel exitLabel = factory.newLabel();
       
    // check the counter and potentially loop
    factory.addLabel(checkLabel);
    factory.addStatement(
        new IfStatement(factory.translateSimpleExpression(context, condition),
            bodyLabel, exitLabel));
    
    // start the body here
    factory.addLabel(bodyLabel);

    LoopContext loopContext = new LoopContext(context, checkLabel, exitLabel);
    factory.translateStatements(loopContext, body);
    
    // increment the counter
    factory.addStatement(new GotoStatement(checkLabel));
    
    factory.addLabel(exitLabel);
  }
  
}
