/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.sexp.FunctionCall;


public class RepeatTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context,
                                          FunctionCall call) {
    addStatement(builder, context, call);
    return Constant.NULL;
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    IRLabel beginLabel = builder.addLabel();
    IRLabel exitLabel = builder.newLabel();
    
    LoopContext loopContext = new LoopContext(context, beginLabel, exitLabel);
    builder.translateStatements(loopContext, call.getArgument(0));
    
    builder.addStatement(new GotoStatement(beginLabel));
    builder.addLabel(exitLabel);
  }
}
