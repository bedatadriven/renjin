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


import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PrimitiveFunction;

import java.util.List;

class BuiltinTranslator extends FunctionCallTranslator {

  private final PrimitiveFunction builtin;

  public BuiltinTranslator(PrimitiveFunction function) {
    this.builtin = function;
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {


    List<IRArgument> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new BuiltinCall(builder.getRuntimeState(), call, builtin.getName(), arguments,
        findForwardedArgumentIndex(context, call));
  }

  static int findForwardedArgumentIndex(TranslationContext context, FunctionCall call) {
    if(context.isEllipsesArgumentKnown()) {
      return -1;
    }
    return call.findEllipsisArgumentIndex();
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                FunctionCall getterCall,
                                                Expression rhs) {

    List<IRArgument> arguments = builder.translateArgumentList(context, getterCall.getArguments());
    arguments.add(new IRArgument("value", builder.simplify(rhs)));

    return new BuiltinCall(builder.getRuntimeState(), getterCall, builtin.getName(), arguments,
        findForwardedArgumentIndex(context, getterCall));
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, call)));
  }
}
