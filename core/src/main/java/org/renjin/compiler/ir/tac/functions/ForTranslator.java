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

import org.renjin.compiler.builtins.LengthSpecializer;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.GotoStatement;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collections;

public class ForTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addForLoop(builder, context, call);
    
    return Constant.NULL;
  }


  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    addForLoop(builder, context, call);
  }
 
  private void addForLoop(IRBodyBuilder factory, TranslationContext context, FunctionCall call) {

    Expression vector =
        factory.translateSimpleExpression(context, call.getArgument(1));

    // initialize the counter
    LocalVariable counter = factory.newLocalVariable("i");
    factory.addStatement(new Assignment(counter, new Constant(0)));

    buildLoop(context, factory, call, vector, counter);
  }

  public static void buildLoop(TranslationContext parentContext, IRBodyBuilder factory, FunctionCall call, Expression vector, LValue counter) {
    Symbol symbol = call.getArgument(0);
    Temp length = factory.newTemp();

    Variable elementVariable = factory.getEnvironmentVariable(symbol);


    SEXP body = call.getArgument(2);

    IRLabel counterLabel = factory.newLabel();
    IRLabel bodyLabel = factory.newLabel();
    IRLabel nextLabel = factory.newLabel();
    IRLabel exitLabel = factory.newLabel();

    factory.addStatement(new Assignment(length, new BuiltinCall(factory.getRuntimeState(), "length",
        new LengthSpecializer(), Collections.singletonList(new IRArgument(vector)))));

    // check the counter and potentially loop
    factory.addLabel(counterLabel);
    factory.addStatement(new IfStatement(new CmpGE(counter, length), exitLabel, bodyLabel));

    // start the body here
    factory.addLabel(bodyLabel);
    factory.addStatement(new Assignment(elementVariable, new ElementAccess(vector, counter)));

    LoopContext loopContext = new LoopContext(parentContext, nextLabel, exitLabel);
    factory.translateStatements(loopContext, body);

    // increment the counter
    factory.addLabel(nextLabel);
    factory.addStatement(new Assignment(counter, new IncrementCounter(counter)));
    factory.addStatement(new GotoStatement(counterLabel));

    factory.addLabel(exitLabel);
  }
}
