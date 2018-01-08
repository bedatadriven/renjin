/**
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

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;


/**
 * Translator for the {@code function} function.
 */
public class ClosureTranslator extends FunctionCallTranslator {

  @Override
  public Expression translateToExpression(IRBodyBuilder builder,
                                          TranslationContext context, Function resolvedFunction, FunctionCall call) {
   
    PairList formals = EvalException.checkedCast(call.getArgument(0));
    SEXP body = call.getArgument(1);
    SEXP source = call.getArgument(2);

    throw new NotCompilableException(call);
  }

  @Override
  public void addStatement(IRBodyBuilder builder, TranslationContext context,
                           Function resolvedFunction, FunctionCall call) {

    // a closure whose value is not used has no side effects
    // E.g.
    // function(x) x*2
    // has no effect.
  }

}
