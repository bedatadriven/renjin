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

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.BuiltinCall;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.ExprStatement;
import org.renjin.eval.EvalException;
import org.renjin.primitives.special.DollarFunction;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.util.Arrays;


public class DollarTranslator extends FunctionCallTranslator {

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    builder.addStatement(new ExprStatement(translateToExpression(builder, context, resolvedFunction, call)));
  }

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
    Expression object = builder.translateExpression(context, call.getArgument(0));

    SEXP nameArgument = call.getArgument(1);
    StringVector name;
    try {
      name = DollarFunction.parseNameArgument(nameArgument);
    } catch (EvalException e) {
      throw new InvalidSyntaxException(e.getMessage());
    }

    return new BuiltinCall(builder.getRuntimeState(), call, "$", Arrays.asList(
        new IRArgument(object),
        new IRArgument(new Constant(name))));
  }
}
