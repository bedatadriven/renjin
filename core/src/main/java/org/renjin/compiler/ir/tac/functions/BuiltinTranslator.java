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


import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PrimitiveFunction;

import java.util.List;

class BuiltinTranslator extends FunctionCallTranslator {

  public static final BuiltinTranslator INSTANCE = new BuiltinTranslator();

  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(call);
    }
    List<IRArgument> arguments = builder.translateArgumentList(context, call.getArguments());
    
    return new BuiltinCall(builder.getRuntimeState(), call, entry, arguments);
  }

  @Override
  public Expression translateToSetterExpression(IRBodyBuilder builder, TranslationContext context,
                                                Function resolvedFunction, FunctionCall getterCall,
                                                Expression rhs) {

    Primitives.Entry entry = Primitives.getBuiltinEntry(((PrimitiveFunction) resolvedFunction).getName());
    if(entry == null) {
      throw new NotCompilableException(getterCall);
    }
    
    List<IRArgument> arguments = builder.translateArgumentList(context, getterCall.getArguments());
    arguments.add(new IRArgument("value", rhs));

    return new BuiltinCall(builder.getRuntimeState(), getterCall, entry, arguments);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
