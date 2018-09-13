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


import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PrimitiveFunction;

import java.util.List;

class BuiltinTranslator extends FunctionCallTranslator {

  public static final BuiltinTranslator INSTANCE = new BuiltinTranslator();

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    String functionName = ((PrimitiveFunction) resolvedFunction).getName();

    List<IRArgument> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new BuiltinCall(builder.getRuntimeState(), call, functionName, arguments);
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                Function resolvedFunction, FunctionCall getterCall,
                                                Expression rhs) {

    String functionName = ((PrimitiveFunction) resolvedFunction).getName();

    List<IRArgument> arguments = builder.translateArgumentList(context, getterCall.getArguments());
    arguments.add(new IRArgument("value", builder.simplify(rhs)));

    return new BuiltinCall(builder.getRuntimeState(), getterCall, functionName, arguments);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
