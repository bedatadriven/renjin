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

import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.ApplyExpression;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.eval.MatchedArguments;
import org.renjin.primitives.special.SapplyFunction;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;

public class SapplyTranslator extends FunctionCallTranslator {
  @Override
  public Expression translateToExpression(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {

    MatchedArguments matched = SapplyFunction.MATCHER.match(call.getArguments());
    SEXP vector = matched.getActualForFormal(0);
    SEXP function = matched.getActualForFormal(1);
    InlinedFunction inlinedFunction = ListApplyTranslator.resolveApplyFunction(builder, call, function);

    SimpleExpression simplify = builder.translateSimpleExpression(context,
        matched.getActualForFormal(3, LogicalVector.TRUE));

    SimpleExpression useNames = builder.translateSimpleExpression(context,
        matched.getActualForFormal(4, LogicalVector.TRUE));

    return new ApplyExpression(builder.translateSimpleExpression(context, vector), inlinedFunction, simplify, useNames);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context, Function resolvedFunction, FunctionCall call) {
    throw new UnsupportedOperationException("TODO");
  }
}
