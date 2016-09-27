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

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;


public class InternalCallTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    SEXP argument = call.getArgument(0);
    if(!(argument instanceof FunctionCall)) {
      throw new InvalidSyntaxException(".Internal() expects a language object as its only argument");
    }
    
    FunctionCall primitiveCall = (FunctionCall) argument;
    if(!(primitiveCall.getFunction() instanceof Symbol)) {
      throw new InvalidSyntaxException("Invalid .Internal() argument");
    }

    Symbol internalName = (Symbol) primitiveCall.getFunction();
    Primitives.Entry entry = Primitives.getInternalEntry(internalName);
    if(entry == null) {
      throw new InvalidSyntaxException("No such .Internal function '" + internalName + "'");
    }
    
    List<IRArgument> internalArguments = builder.translateArgumentList(context, primitiveCall.getArguments());

    return new BuiltinCall(builder.getRuntimeState(), call, entry, internalArguments);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {
    builder.addStatement(
        new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }
}
